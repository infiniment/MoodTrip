package com.moodTrip.spring.domain.enteringRoom.service;

import com.moodTrip.spring.domain.enteringRoom.dto.request.JoinRequest;
import com.moodTrip.spring.domain.enteringRoom.dto.response.JoinResponse;
import com.moodTrip.spring.domain.enteringRoom.entity.EnteringRoom;
import com.moodTrip.spring.domain.enteringRoom.repository.JoinRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoinService {

    private final JoinRepository joinRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final ProfileRepository profileRepository; // 새로 추가
    private final SecurityUtil securityUtil;

    /**
     * 방 입장 신청하기 - 자기소개 포함 버전
     */
    @Transactional
    public JoinResponse applyToRoom(Long roomId, JoinRequest request) {
        log.info("방 입장 신청 시작 - roomId: {}, 메시지 길이: {}글자",
                roomId, request.getMessage() != null ? request.getMessage().length() : 0);

        try {
            // 1. 현재 로그인한 사용자 가져오기
            Member currentMember = securityUtil.getCurrentMember();
            log.info("신청자: {}", currentMember.getNickname());

            // 2. 방 정보 조회
            Room targetRoom = roomRepository.findById(roomId)
                    .orElseThrow(() -> {
                        log.error("존재하지 않는 방 - roomId: {}", roomId);
                        return new RuntimeException("존재하지 않는 방입니다.");
                    });

            log.info("신청 대상 방: {} (현재 {}/{}명)",
                    targetRoom.getRoomName(),
                    targetRoom.getRoomCurrentCount(),
                    targetRoom.getRoomMaxCount());

            // 3. 유효성 검사
            validateRoomApplication(currentMember, targetRoom);

            // 4. 프로필에서 자기소개 가져오기
            String profileBio = getProfileBio(currentMember);
            log.info("신청자 자기소개 조회 완료 - 길이: {}글자", profileBio.length());

            // 5. 메시지 조합: "자기소개: (내용)" + 입장 신청 메시지
            String combinedMessage = buildCombinedMessage(profileBio, request.getMessage());
            log.info("최종 메시지 조합 완료 - 길이: {}글자", combinedMessage.length());

            // 6. 신청 엔티티 생성 및 저장
            EnteringRoom enteringRoom = EnteringRoom.builder()
                    .applicant(currentMember)
                    .room(targetRoom)
                    .message(combinedMessage) // 조합된 메시지 사용
                    .status(EnteringRoom.EnteringStatus.PENDING)
                    .build();

            EnteringRoom savedApplication = joinRepository.save(enteringRoom);

            log.info("방 입장 신청 완료 - 신청ID: {}, 방: {}, 신청자: {}",
                    savedApplication.getEnteringRoomId(), targetRoom.getRoomName(), currentMember.getNickname());

            // 7. 성공 응답 반환
            return JoinResponse.builder()
                    .joinRequestId(savedApplication.getEnteringRoomId())
                    .roomId(roomId)
                    .applicantNickname(currentMember.getNickname())
                    .message(combinedMessage)
                    .appliedAt(savedApplication.getCreatedAt())
                    .status("PENDING")
                    .resultMessage("입장 신청이 완료되었습니다! 방장의 승인을 기다려주세요.")
                    .success(true)
                    .build();

        } catch (RuntimeException e) {
            log.error("방 입장 신청 실패 - roomId: {}, 신청자: {}, 오류: {}",
                    roomId, securityUtil.getCurrentNickname(), e.getMessage());

            return JoinResponse.builder()
                    .joinRequestId(null)
                    .roomId(roomId)
                    .applicantNickname(securityUtil.getCurrentNickname())
                    .message(request.getMessage())
                    .appliedAt(LocalDateTime.now())
                    .status("REJECTED")
                    .resultMessage(e.getMessage())
                    .success(false)
                    .build();

        } catch (Exception e) {
            log.error("예상치 못한 오류 발생 - roomId: {}", roomId, e);

            return JoinResponse.builder()
                    .joinRequestId(null)
                    .roomId(roomId)
                    .message(request.getMessage())
                    .appliedAt(LocalDateTime.now())
                    .status("ERROR")
                    .resultMessage("신청 처리 중 오류가 발생했습니다. 고객센터에 문의해주세요.")
                    .success(false)
                    .build();
        }
    }

    /**
     * 현재 사용자의 프로필에서 자기소개 가져오기
     * SecurityUtil에서 프로필 자동 생성하므로 항상 존재함
     */
    private String getProfileBio(Member member) {
        Profile profile = profileRepository.findByMember(member)
                .orElseThrow(() -> new RuntimeException("프로필을 찾을 수 없습니다."));

        log.info("프로필 자기소개 조회 성공 - 회원: {}", member.getNickname());
        return profile.getProfileBio();
    }

    private String buildCombinedMessage(String profileBio, String applicationMessage) {
        StringBuilder combinedMessage = new StringBuilder();

        // 자기소개 섹션 (제목 + 내용 한 줄에 붙임)
        combinedMessage.append("자기소개: ").append(profileBio);

        // 신청 내용이 있는 경우만 추가
        if (applicationMessage != null && !applicationMessage.trim().isEmpty()) {
            combinedMessage.append("\n"); // 한 줄만 뛰기
            combinedMessage.append("신청 내용: ");
            combinedMessage.append(applicationMessage.trim());
        }

        return combinedMessage.toString();
    }


    /**
     * 방 신청 유효성 검사 (기존 코드 그대로)
     */
    private void validateRoomApplication(Member applicant, Room room) {
        log.info("방 신청 유효성 검사 시작 - 방: {}, 신청자: {}",
                room.getRoomName(), applicant.getNickname());

        // 1. 자기 방에 신청하는지 체크
        if (room.getCreator().getMemberPk().equals(applicant.getMemberPk())) {
            log.warn("자신이 만든 방 신청 시도 - 방장: {}", applicant.getNickname());
            throw new RuntimeException("자신이 만든 방에는 신청할 수 없습니다.");
        }

        // 2. 방이 이미 가득 찼는지 체크
        Long currentActiveParticipants = roomMemberRepository.countByRoomAndIsActiveTrue(room);

        if (currentActiveParticipants >= room.getRoomMaxCount()) {
            log.warn("방 정원 초과로 신청 거부 - 방: {}, 현재/최대: {}/{}",
                    room.getRoomName(), currentActiveParticipants, room.getRoomMaxCount());

            throw new RuntimeException(
                    String.format("이미 방이 가득 찼습니다. 다른 방을 찾아보세요! (%d/%d)",
                            currentActiveParticipants, room.getRoomMaxCount())
            );
        }

        // 3. 이미 신청했는지 체크
        boolean alreadyApplied = joinRepository.existsByApplicantAndRoom(applicant, room);
        if (alreadyApplied) {
            log.warn("중복 신청 시도 - 방: {}, 신청자: {}",
                    room.getRoomName(), applicant.getNickname());
            throw new RuntimeException("이미 해당 방에 신청하셨습니다.");
        }

        // 4. 방이 삭제되었는지 체크
        if (room.getIsDeleteRoom() != null && room.getIsDeleteRoom()) {
            log.warn("삭제된 방 신청 시도 - 방: {}, 신청자: {}",
                    room.getRoomName(), applicant.getNickname());
            throw new RuntimeException("삭제된 방에는 신청할 수 없습니다.");
        }

        // 5. 이미 해당 방의 참여자인지 체크
        boolean alreadyParticipating = roomMemberRepository
                .findByMemberAndRoom(applicant, room)
                .map(roomMember -> roomMember.getIsActive())
                .orElse(false);

        if (alreadyParticipating) {
            log.warn("이미 참여중인 방 신청 시도 - 방: {}, 신청자: {}",
                    room.getRoomName(), applicant.getNickname());
            throw new RuntimeException("이미 참여중인 방입니다.");
        }

        log.info("유효성 검사 통과 - 방: {} ({}/{}), 신청자: {}",
                room.getRoomName(), currentActiveParticipants, room.getRoomMaxCount(), applicant.getNickname());
    }
}