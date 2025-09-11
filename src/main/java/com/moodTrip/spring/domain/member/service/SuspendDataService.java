package com.moodTrip.spring.domain.member.service;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
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

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuspendDataService {

    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final JoinRepository joinRepository;

    /**
     * 회원 정지 처리 - 탈퇴와 동일한 로직으로 모든 곳에서 제거
     */
    @Transactional
    public void suspendMember(Member member) {
        log.info("=== 회원 정지 처리 시작 - 회원ID: {} ===", member.getMemberId());

        try {
            // 정지 전 데이터 백업 (복구용)
            Map<String, Object> backupData = backupMemberData(member);

            // 1단계: 입장 신청 데이터 완전 삭제 (탈퇴와 동일)
            deleteJoinRequests(member);

            // 2단계: 방 참여 데이터 완전 삭제 + 방 인원수 조정 (탈퇴와 동일)
            leaveAllRooms(member);

            // 3단계: 본인이 만든 방 처리 (탈퇴와 동일)
            handleCreatedRooms(member);

            // 4단계: Member 상태를 SUSPENDED로 변경
            member.setStatus(Member.MemberStatus.SUSPENDED);
            memberRepository.save(member);

            // 5단계: 백업 데이터 저장 (복구 시 사용)
            saveBackupData(member, backupData);

            log.info("=== 회원 정지 처리 완료 - 회원ID: {} ===", member.getMemberId());

        } catch (Exception e) {
            log.error("회원 정지 처리 중 오류 발생 - 회원ID: {}", member.getMemberId(), e);
            throw new RuntimeException("정지 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 회원 정지 해제 및 데이터 복구
     */
    @Transactional
    public void reactivateMember(Member member) {
        log.info("=== 회원 정지 해제 처리 시작 - 회원ID: {} ===", member.getMemberId());

        try {
            // 1단계: Member 상태를 ACTIVE로 변경
            member.setStatus(Member.MemberStatus.ACTIVE);
            memberRepository.save(member);

            // 2단계: 백업 데이터 복구 (필요시 구현)
            // restoreBackupData(member);

            log.info("=== 회원 정지 해제 완료 - 회원ID: {} ===", member.getMemberId());

        } catch (Exception e) {
            log.error("회원 정지 해제 중 오류 발생 - 회원ID: {}", member.getMemberId(), e);
            throw new RuntimeException("정지 해제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 정지 전 데이터 백업 (복구용)
     */
    private Map<String, Object> backupMemberData(Member member) {
        Map<String, Object> backup = new HashMap<>();

        // 참여 중인 방 목록 백업
        List<RoomMember> participatingRooms = roomMemberRepository.findByMemberAndIsActiveTrue(member);
        backup.put("participatingRooms", participatingRooms);

        // 입장 신청 목록 백업
        List<EnteringRoom> joinRequests = joinRepository.findByApplicant(member);
        backup.put("joinRequests", joinRequests);

        // 만든 방 목록 백업
        List<Room> createdRooms = roomRepository.findByCreatorAndIsDeleteRoomFalse(member);
        backup.put("createdRooms", createdRooms);

        log.info("데이터 백업 완료 - 회원ID: {}, 참여방: {}개, 신청: {}개, 생성방: {}개",
                member.getMemberId(), participatingRooms.size(), joinRequests.size(), createdRooms.size());

        return backup;
    }

    /**
     * 백업 데이터 저장 (실제 구현시 별도 테이블에 저장)
     */
    private void saveBackupData(Member member, Map<String, Object> backupData) {
        // TODO: 실제 구현시 별도의 백업 테이블에 저장
        // 현재는 로그로만 기록
        log.info("백업 데이터 저장 완료 - 회원ID: {}", member.getMemberId());
    }

    // === 이하는 WithdrawDataService와 동일한 로직 ===

    /**
     * 1단계: 입장 신청 데이터 완전 삭제
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
     */
    private void leaveAllRooms(Member member) {
        log.info("2단계: 방 참여 데이터 삭제 시작 - 회원ID: {}", member.getMemberId());

        try {
            List<RoomMember> participatingRooms = roomMemberRepository.findByMemberAndIsActiveTrue(member);

            for (RoomMember roomMember : participatingRooms) {
                Room room = roomMember.getRoom();

                // 본인이 만든 방이 아닌 경우만 처리
                if (!room.getCreator().getMemberPk().equals(member.getMemberPk())) {
                    roomMemberRepository.delete(roomMember);
                    room.setRoomCurrentCount(room.getRoomCurrentCount() - 1);
                    roomRepository.save(room);

                    log.info("방 '{}' 참여 해제 완료, 인원: {}/{}",
                            room.getRoomName(), room.getRoomCurrentCount(), room.getRoomMaxCount());
                }
            }

        } catch (Exception e) {
            log.error("방 참여 데이터 삭제 실패", e);
            throw new RuntimeException("방 참여 데이터 삭제 중 오류 발생");
        }
    }

    /**
     * 3단계: 본인이 만든 방 처리
     */
    private void handleCreatedRooms(Member member) {
        log.info("3단계: 본인 방 처리 시작 - 회원ID: {}", member.getMemberId());

        try {
            List<Room> myRooms = roomRepository.findByCreatorAndIsDeleteRoomFalse(member);

            for (Room room : myRooms) {
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
     */

    private void transferRoomOwnership(Room room, Member newOwner) {
        log.info("방장 이양 시작 - 방: '{}', 신규 방장: '{}'", room.getRoomName(), newOwner.getNickname());

        try {
            Member oldOwner = room.getCreator(); // ✅ 기존 방장 저장(중요)

            room.setCreator(newOwner);

            RoomMember newLeaderMember = roomMemberRepository.findByMemberAndRoom(newOwner, room)
                    .orElseThrow(() -> new RuntimeException("신규 방장의 RoomMember를 찾을 수 없습니다"));
            newLeaderMember.setRole("LEADER");
            roomMemberRepository.save(newLeaderMember);

            // ✅ 기존 방장의 RoomMember를 삭제 (oldOwner 사용)
            roomMemberRepository.findByMemberAndRoom(oldOwner, room)
                    .ifPresent(roomMemberRepository::delete);

            roomRepository.save(room);
            log.info("방장 이양 완료 - 방: '{}', 신규 방장: '{}'", room.getRoomName(), newOwner.getNickname());

        } catch (Exception e) {
            log.error("방장 이양 실패 - 방: '{}'", room.getRoomName(), e);
            throw new RuntimeException("방장 이양 중 오류 발생");
        }
    }

//    private void transferRoomOwnership(Room room, Member newOwner) {
//        log.info("방장 이양 시작 - 방: '{}', 신규 방장: '{}'", room.getRoomName(), newOwner.getNickname());
//
//        try {
//            room.setCreator(newOwner);
//
//            RoomMember newLeaderMember = roomMemberRepository.findByMemberAndRoom(newOwner, room)
//                    .orElseThrow(() -> new RuntimeException("신규 방장의 RoomMember를 찾을 수 없습니다"));
//            newLeaderMember.setRole("LEADER");
//            roomMemberRepository.save(newLeaderMember);
//
//            roomMemberRepository.findByMemberAndRoom(room.getCreator(), room)
//                    .ifPresent(roomMemberRepository::delete);
//
//            roomRepository.save(room);
//
//            log.info("방장 이양 완료 - 방: '{}', 신규 방장: '{}'", room.getRoomName(), newOwner.getNickname());
//
//        } catch (Exception e) {
//            log.error("방장 이양 실패 - 방: '{}'", room.getRoomName(), e);
//            throw new RuntimeException("방장 이양 중 오류 발생");
//        }
//    }

    /**
     * 빈 방 완전 삭제
     */
    private void deleteEmptyRoom(Room room) {
        log.info("빈 방 삭제 시작 - 방: '{}'", room.getRoomName());

        try {
            List<RoomMember> remainingMembers = roomMemberRepository.findByRoom(room);
            if (!remainingMembers.isEmpty()) {
                roomMemberRepository.deleteAll(remainingMembers);
                log.info("방의 남은 RoomMember {}건 삭제", remainingMembers.size());
            }

            roomRepository.delete(room);
            log.info("빈 방 '{}' 완전 삭제 완료", room.getRoomName());

        } catch (Exception e) {
            log.error("빈 방 삭제 실패 - 방: '{}'", room.getRoomName(), e);
            throw new RuntimeException("빈 방 삭제 중 오류 발생");
        }
    }
}