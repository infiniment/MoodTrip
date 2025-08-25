package com.moodTrip.spring.domain.member.service;

import com.moodTrip.spring.domain.member.dto.response.WithdrawResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.enteringRoom.entity.EnteringRoom;
import com.moodTrip.spring.domain.enteringRoom.repository.JoinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawDataService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final JoinRepository joinRepository;

    /**
     * 🔥 메인 탈퇴 처리 메서드
     */
    @Transactional  // 전체를 하나의 트랜잭션으로 처리 (중간에 실패하면 모두 롤백)
    public WithdrawResponse processCompleteWithdraw(Member member) {
        log.info("=== 회원 완전 탈퇴 처리 시작 - 회원ID: {} ===", member.getMemberId());

        try {
            // 1단계: 입장 신청 데이터 완전 삭제
            deleteJoinRequests(member);

            // 2단계: 방 참여 데이터 완전 삭제 + 방 인원수 조정
            leaveAllRooms(member);

            // 3단계: 본인이 만든 방 처리 (방장 이양 또는 방 삭제)
            handleCreatedRooms(member);

            // 4단계: Member 논리적 삭제 (isWithdraw = true)
            member.setIsWithdraw(true);
            memberRepository.save(member);

            // 5단계: Profile은 보존! (재가입 시 복구용)
            preserveProfileForReactivation(member);

            log.info("=== 회원 완전 탈퇴 처리 완료 - 회원ID: {} ===", member.getMemberId());

            return WithdrawResponse.builder()
                    .memberId(member.getMemberId())
                    .withdrawnAt(LocalDateTime.now())
                    .message("탈퇴가 완료되었습니다. 같은 아이디로 재가입하시면 기존 프로필을 복구할 수 있습니다.")
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("회원 탈퇴 처리 중 오류 발생 - 회원ID: {}", member.getMemberId(), e);
            throw new RuntimeException("탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 1단계: 입장 신청 데이터 완전 삭제
     * - 본인이 다른 방에 한 입장 신청들을 모두 삭제
     * - 다른 사람이 본인 방에 한 입장 신청들도 모두 삭제 (방이 사라질 예정이므로)
     */
    private void deleteJoinRequests(Member member) {
        log.info("1단계: 입장 신청 데이터 삭제 시작 - 회원ID: {}", member.getMemberId());

        try {
            // 본인이 다른 방에 한 신청들 삭제
            List<EnteringRoom> myApplications = joinRepository.findByApplicant(member);
            if (!myApplications.isEmpty()) {
                joinRepository.deleteAll(myApplications);
                log.info("본인의 입장 신청 {}건 삭제 완료", myApplications.size());
            }

            // 본인이 만든 방들에 대한 다른 사람의 신청들 삭제
            List<Room> myRooms = roomRepository.findByCreatorAndIsDeleteRoomFalse(member);
            for (Room room : myRooms) {
                List<EnteringRoom> roomApplications = joinRepository.findByRoom(room);
                if (!roomApplications.isEmpty()) {
                    joinRepository.deleteAll(roomApplications);
                    log.info("방 '{}' 입장 신청 {}건 삭제 완료", room.getRoomName(), roomApplications.size());
                }
            }

        } catch (Exception e) {
            log.error("입장 신청 데이터 삭제 실패", e);
            throw new RuntimeException("입장 신청 데이터 삭제 중 오류 발생");
        }
    }

    /**
     * 2단계: 방 참여 데이터 완전 삭제 + 방 인원수 조정
     * - 본인이 참여 중인 다른 사람의 방에서 나가기
     * - 해당 방들의 현재 인원수를 1 감소시키기
     */
    private void leaveAllRooms(Member member) {
        log.info("2단계: 방 참여 데이터 삭제 시작 - 회원ID: {}", member.getMemberId());

        try {
            // 본인이 참여 중인 방들 조회 (본인이 만든 방 제외)
            List<RoomMember> participatingRooms = roomMemberRepository.findByMemberAndIsActiveTrue(member);

            for (RoomMember roomMember : participatingRooms) {
                Room room = roomMember.getRoom();

                // 본인이 만든 방이 아닌 경우만 처리 (본인 방은 3단계에서 별도 처리)
                if (!room.getCreator().getMemberPk().equals(member.getMemberPk())) {

                    // RoomMember 완전 삭제
                    roomMemberRepository.delete(roomMember);

                    // 방의 현재 인원수 1 감소
                    room.setRoomCurrentCount(room.getRoomCurrentCount() - 1);
                    roomRepository.save(room);

                    log.info("방 '{}' 참여 해제 완료, 인원: {}/{}",
                            room.getRoomName(),
                            room.getRoomCurrentCount(),
                            room.getRoomMaxCount());
                }
            }

        } catch (Exception e) {
            log.error("방 참여 데이터 삭제 실패", e);
            throw new RuntimeException("방 참여 데이터 삭제 중 오류 발생");
        }
    }

    /**
     * 3단계: 본인이 만든 방 처리
     * - 다른 참여자가 있으면: 방장 이양 (가장 먼저 들어온 사람에게)
     * - 다른 참여자가 없으면: 방 완전 삭제
     */
    private void handleCreatedRooms(Member member) {
        log.info("3단계: 본인 방 처리 시작 - 회원ID: {}", member.getMemberId());

        try {
            List<Room> myRooms = roomRepository.findByCreatorAndIsDeleteRoomFalse(member);

            for (Room room : myRooms) {
                // 해당 방에 다른 참여자가 있는지 확인
                List<RoomMember> otherMembers = roomMemberRepository.findByRoomAndIsActiveTrue(room)
                        .stream()
                        .filter(rm -> !rm.getMember().getMemberPk().equals(member.getMemberPk()))
                        .toList();

                if (!otherMembers.isEmpty()) {
                    // 다른 참여자가 있으면 방장 이양
                    transferRoomOwnership(room, otherMembers.get(0).getMember());
                } else {
                    // 다른 참여자가 없으면 방 완전 삭제
                    deleteEmptyRoom(room);
                }
            }

        } catch (Exception e) {
            log.error("본인 방 처리 실패", e);
            throw new RuntimeException("방 처리 중 오류 발생");
        }
    }

    /**
     * 방장 이양 처리
     * - 가장 먼저 들어온 참여자에게 방장 권한 넘기기
     * - 기존 방장(탈퇴자)의 RoomMember 삭제
     */
    private void transferRoomOwnership(Room room, Member newOwner) {
        log.info("방장 이양 시작 - 방: '{}', 신규 방장: '{}'",
                room.getRoomName(), newOwner.getNickname());

        try {
            // 방의 creator를 새로운 방장으로 변경
            room.setCreator(newOwner);

            // 새 방장의 role을 LEADER로 변경
            RoomMember newLeaderMember = roomMemberRepository.findByMemberAndRoom(newOwner, room)
                    .orElseThrow(() -> new RuntimeException("새 방장의 RoomMember를 찾을 수 없습니다"));
            newLeaderMember.setRole("LEADER");
            roomMemberRepository.save(newLeaderMember);

            // 기존 방장(탈퇴자)의 RoomMember 삭제
            roomMemberRepository.findByMemberAndRoom(room.getCreator(), room)
                    .ifPresent(roomMemberRepository::delete);

            // 방 정보 저장
            roomRepository.save(room);

            log.info("방장 이양 완료 - 방: '{}', 신규 방장: '{}'",
                    room.getRoomName(), newOwner.getNickname());

        } catch (Exception e) {
            log.error("방장 이양 실패 - 방: '{}'", room.getRoomName(), e);
            throw new RuntimeException("방장 이양 중 오류 발생");
        }
    }

    /**
     * 빈 방 완전 삭제
     * - 참여자가 없는 방은 완전히 삭제
     */
    private void deleteEmptyRoom(Room room) {
        log.info("빈 방 삭제 시작 - 방: '{}'", room.getRoomName());

        try {
            // 혹시 남아있을 수 있는 RoomMember들 먼저 삭제
            List<RoomMember> remainingMembers = roomMemberRepository.findByRoom(room);
            if (!remainingMembers.isEmpty()) {
                roomMemberRepository.deleteAll(remainingMembers);
                log.info("방의 남은 RoomMember {}건 삭제", remainingMembers.size());
            }

            // Room 완전 삭제
            roomRepository.delete(room);
            log.info("빈 방 '{}' 완전 삭제 완료", room.getRoomName());

        } catch (Exception e) {
            log.error("빈 방 삭제 실패 - 방: '{}'", room.getRoomName(), e);
            throw new RuntimeException("빈 방 삭제 중 오류 발생");
        }
    }

    /**
     * 4단계: Profile 보존 처리
     * - Profile은 삭제하지 않고 그대로 유지
     * - 재가입 시 복구할 수 있도록 보존
     */
    private void preserveProfileForReactivation(Member member) {
        log.info("4단계: Profile 보존 처리 - 회원ID: {}", member.getMemberId());

        try {
            Profile profile = profileRepository.findByMember(member).orElse(null);

            if (profile != null) {
                log.info("Profile 보존 완료 - 회원ID: {}, Profile ID: {}, 닉네임: '{}', 자기소개: {}글자",
                        member.getMemberId(),
                        profile.getProfileId(),
                        member.getNickname(),
                        profile.getProfileBio() != null ? profile.getProfileBio().length() : 0);

                // Profile은 그대로 두고 로그만 남김
                // 재가입 시 이 Profile을 다시 활성화할 예정
            } else {
                log.info("보존할 Profile이 없음 - 회원ID: {}", member.getMemberId());
            }

        } catch (Exception e) {
            log.error("Profile 보존 처리 실패", e);
            // Profile 보존 실패는 탈퇴 전체를 실패시키지 않음 (중요하지 않은 작업)
            log.warn("Profile 보존 실패했지만 탈퇴는 계속 진행");
        }
    }

    /**
     * 🔄 재가입 시 계정 복구 메서드
     * - 같은 아이디(memberId)로 재가입할 때 기존 Member + Profile 복구
     */
    @Transactional
    public Member reactivateAccount(String memberId) {
        log.info("계정 복구 시작 - 회원ID: {}", memberId);

        try {
            // 탈퇴한 상태의 기존 회원 찾기
            Member withdrawnMember = memberRepository.findByMemberIdAndIsWithdrawTrue(memberId)
                    .orElse(null);

            if (withdrawnMember != null) {
                // 기존 계정 복구
                withdrawnMember.setIsWithdraw(false);  // 탈퇴 상태 해제
                Member reactivatedMember = memberRepository.save(withdrawnMember);

                log.info("기존 계정 복구 완료 - 회원ID: {}, 닉네임: '{}', 이메일: {}",
                        reactivatedMember.getMemberId(),
                        reactivatedMember.getNickname(),
                        reactivatedMember.getEmail());

                return reactivatedMember;

            } else {
                log.info("복구할 탈퇴 계정이 없음 - 새 계정으로 가입 필요 - 회원ID: {}", memberId);
                return null;  // 새로 가입해야 함
            }

        } catch (Exception e) {
            log.error("계정 복구 실패 - 회원ID: {}", memberId, e);
            throw new RuntimeException("계정 복구 중 오류 발생");
        }
    }

    /**
     * 🔍 재가입 가능 여부 확인
     * - 해당 아이디(memberId)로 탈퇴한 계정이 있는지 확인
     */
    public boolean canReactivate(String memberId) {
        try {
            return memberRepository.existsByMemberIdAndIsWithdrawTrue(memberId);
        } catch (Exception e) {
            log.error("재가입 가능 여부 확인 실패", e);
            return false;
        }
    }

    // WithdrawDataService.java에 추가
    /**
     * 소셜 계정 재가입 가능 여부 확인
     */
    public boolean canReactivateSocial(String provider, String providerId) {
        try {
            return memberRepository.existsByProviderAndProviderIdAndIsWithdrawTrue(provider, providerId);
        } catch (Exception e) {
            log.error("소셜 재가입 가능 여부 확인 실패", e);
            return false;
        }
    }

    /**
     * 소셜 계정 복구
     */
    @Transactional
    public Member reactivateSocialAccount(String provider, String providerId) {
        log.info("소셜 계정 복구 시작 - Provider: {}, ProviderId: {}", provider, providerId);

        try {
            Member withdrawnMember = memberRepository.findByProviderAndProviderIdAndIsWithdrawTrue(provider, providerId)
                    .orElse(null);

            if (withdrawnMember != null) {
                // 기존 계정 복구
                withdrawnMember.setIsWithdraw(false);
                Member reactivatedMember = memberRepository.save(withdrawnMember);

                log.info("기존 소셜 계정 복구 완료 - 회원ID: {}, Provider: {}",
                        reactivatedMember.getMemberId(), provider);

                return reactivatedMember;
            } else {
                log.info("복구할 탈퇴 소셜 계정이 없음");
                return null;
            }

        } catch (Exception e) {
            log.error("소셜 계정 복구 실패", e);
            throw new RuntimeException("소셜 계정 복구 중 오류 발생");
        }
    }
}