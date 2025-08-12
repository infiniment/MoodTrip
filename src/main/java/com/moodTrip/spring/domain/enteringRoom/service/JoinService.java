// 📁 src/main/java/com/moodTrip/spring/domain/enteringRoom/service/JoinService.java
package com.moodTrip.spring.domain.enteringRoom.service;

import com.moodTrip.spring.domain.enteringRoom.dto.request.JoinRequest;
import com.moodTrip.spring.domain.enteringRoom.dto.response.JoinResponse;
import com.moodTrip.spring.domain.enteringRoom.entity.EnteringRoom;
import com.moodTrip.spring.domain.enteringRoom.repository.JoinRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
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

    private final JoinRepository joinRepository;  // 🔥 이름 변경!
    private final RoomRepository roomRepository;
    private final SecurityUtil securityUtil;

    /**
     * 방 입장 신청하기
     */
    @Transactional
    public JoinResponse applyToRoom(Long roomId, JoinRequest request) {
        log.info("방 입장 신청 시작 - roomId: {}", roomId);

        try {
            // 1️⃣ 현재 로그인한 사용자 가져오기
            Member currentMember = securityUtil.getCurrentMember();
            log.info("신청자: {}", currentMember.getNickname());

            // 2️⃣ 방 정보 조회
            Room targetRoom = roomRepository.findById(roomId)
                    .orElseThrow(() -> {
                        log.error("존재하지 않는 방 - roomId: {}", roomId);
                        return new RuntimeException("존재하지 않는 방입니다.");
                    });

            // 3️⃣ 유효성 검사들
            validateRoomApplication(currentMember, targetRoom);

            // 4️⃣ 신청 엔티티 생성 및 저장
            EnteringRoom enteringRoom = EnteringRoom.builder()
                    .applicant(currentMember)
                    .room(targetRoom)
                    .message(request.getMessage())
                    .status(EnteringRoom.EnteringStatus.PENDING)
                    .build();

            EnteringRoom savedApplication = joinRepository.save(enteringRoom);  // 🔥 이름 변경!

            log.info("방 입장 신청 완료 - 신청ID: {}, 방: {}, 신청자: {}",
                    savedApplication.getEnteringRoomId(), targetRoom.getRoomName(), currentMember.getNickname());

            // 5️⃣ 성공 응답 반환
            return JoinResponse.builder()
                    .joinRequestId(savedApplication.getEnteringRoomId())
                    .resultMessage("입장 신청이 완료되었습니다! 방장의 승인을 기다려주세요.")
                    .success(true)
                    .appliedAt(savedApplication.getCreatedAt())
                    .build();

        } catch (RuntimeException e) {
            log.error("방 입장 신청 실패 - roomId: {}, 오류: {}", roomId, e.getMessage());

            return JoinResponse.builder()
                    .joinRequestId(null)
                    .resultMessage(e.getMessage())
                    .success(false)
                    .appliedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("예상치 못한 오류 발생", e);

            return JoinResponse.builder()
                    .joinRequestId(null)
                    .resultMessage("신청 처리 중 오류가 발생했습니다. 고객센터에 문의해주세요.")
                    .success(false)
                    .appliedAt(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 방 신청 유효성 검사
     */
    private void validateRoomApplication(Member applicant, Room room) {

        // 1️⃣ 자기 방에 신청하는지 체크
        if (room.getCreator().getMemberPk().equals(applicant.getMemberPk())) {
            throw new RuntimeException("자신이 만든 방에는 신청할 수 없습니다.");
        }

        // 2️⃣ 이미 신청했는지 체크 (중복 신청 방지)
        boolean alreadyApplied = joinRepository.existsByApplicantAndRoom(applicant, room);  // 🔥 이름 변경!
        if (alreadyApplied) {
            throw new RuntimeException("이미 해당 방에 신청하셨습니다.");
        }

        // 3️⃣ 방이 삭제되었는지 체크
        if (room.getIsDeleteRoom() != null && room.getIsDeleteRoom()) {
            throw new RuntimeException("삭제된 방에는 신청할 수 없습니다.");
        }

        // 4️⃣ 방이 이미 가득 찼는지 체크
        Long approvedCount = joinRepository.countApprovedByRoom(room);  // 🔥 이름 변경!
        if (approvedCount >= room.getRoomMaxCount()) {
            throw new RuntimeException("해당 방은 이미 정원이 가득 찼습니다.");
        }

        log.info("유효성 검사 통과 - 방: {}, 신청자: {}", room.getRoomName(), applicant.getNickname());
    }
}