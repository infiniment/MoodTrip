package com.moodTrip.spring.domain.enteringRoom.service;

import com.moodTrip.spring.domain.enteringRoom.dto.response.ActionResponse;
import com.moodTrip.spring.domain.enteringRoom.dto.response.JoinRequestListResponse;
import com.moodTrip.spring.domain.enteringRoom.dto.response.RequestStatsResponse;
import com.moodTrip.spring.domain.enteringRoom.dto.response.RoomWithRequestsResponse;
import com.moodTrip.spring.domain.enteringRoom.entity.EnteringRoom;
import com.moodTrip.spring.domain.enteringRoom.repository.JoinRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoinRequestManagementService {

    // 방 신청하기 관련 dto들을 활용한 서비스
    private final JoinRepository joinRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final SecurityUtil securityUtil;

    // 방장의 모든 방과 각 방의 신청 목록 조회
    public List<RoomWithRequestsResponse> getMyRoomsWithRequests() {
        log.info("방장의 방 목록 + 신청 목록 조회 시작");

        try {
            // 현재 로그인한 방장 정보 가져오기
            Member roomOwner = securityUtil.getCurrentMember();
            log.info("방장: {}", roomOwner.getNickname());

            // 방장이 만든 방들 조회
            List<Room> myRooms = roomRepository.findByCreatorAndIsDeleteRoomFalse(roomOwner);
            log.info("방장이 만든 방 개수: {}", myRooms.size());

            // 각 방별로 신청 목록 조회하여 DTO 변환
            return myRooms.stream()
                    .map(this::convertToRoomWithRequests)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("방 목록 + 신청 목록 조회 실패", e);
            throw new RuntimeException("방 목록을 불러올 수 없습니다.");
        }
    }

    // 특정 방의 신청 목록만 조회
    public List<JoinRequestListResponse> getRoomRequests(Long roomId) {
        log.info("방 신청 목록 조회 - roomId: {}", roomId);

        Room room = validateRoomOwnership(roomId);

        List<EnteringRoom> pendingRequests = joinRepository.findByRoomIdWithProfile(roomId)
                .stream()
                .filter(er -> er.getStatus() == EnteringRoom.EnteringStatus.PENDING)
                .toList();

        return pendingRequests.stream()
                .map(JoinRequestListResponse::from)
                .toList();
    }

    // 개별 신청 승인
    @Transactional
    public ActionResponse approveRequest(Long requestId) {
        log.info("개별 신청 승인 시작 - requestId: {}", requestId);

        try {
            // 신청 정보 조회 및 권한 확인
            EnteringRoom request = validateRequestOwnership(requestId);
            String applicantName = request.getApplicant().getNickname();

            // 이미 처리된 신청인지 확인
            if (request.getStatus() != EnteringRoom.EnteringStatus.PENDING) {
                return ActionResponse.failure("이미 처리된 신청입니다.");
            }

            // 방 정원 확인
            Room room = request.getRoom();
            Long currentApprovedCount = joinRepository.countApprovedByRoom(room);
            if (currentApprovedCount >= room.getRoomMaxCount()) {
                return ActionResponse.failure("방 정원이 가득 찼습니다.");
            }

            // 신청 승인 처리
            request.setStatus(EnteringRoom.EnteringStatus.APPROVED);

            // RoomMember 테이블에 실제 참여자로 추가
            addApprovedMemberToRoom(request);

            log.info("개별 신청 승인 완료 - 신청자: {}, 방: {}", applicantName, room.getRoomName());

            return ActionResponse.success(
                    applicantName + "님의 입장을 승인했습니다.",
                    List.of(applicantName)
            );

        } catch (RuntimeException e) {
            log.error("개별 신청 승인 실패 - requestId: {}, 오류: {}", requestId, e.getMessage());
            return ActionResponse.failure(e.getMessage());

        } catch (Exception e) {
            log.error("예상치 못한 오류", e);
            return ActionResponse.failure("신청 처리 중 오류가 발생했습니다.");
        }
    }

    // 개별 신청 거절
    @Transactional
    public ActionResponse rejectRequest(Long requestId) {
        log.info("개별 신청 거절 시작 - requestId: {}", requestId);

        try {
            // 신청 정보 조회 및 권한 확인
            EnteringRoom request = validateRequestOwnership(requestId);
            String applicantName = request.getApplicant().getNickname();

            // 이미 처리된 신청인지 확인
            if (request.getStatus() != EnteringRoom.EnteringStatus.PENDING) {
                return ActionResponse.failure("이미 처리된 신청입니다.");
            }

            // 신청 거절 처리
            request.setStatus(EnteringRoom.EnteringStatus.REJECTED);

            log.info("개별 신청 거절 완료 - 신청자: {}, 방: {}", applicantName, request.getRoom().getRoomName());

            return ActionResponse.success(
                    applicantName + "님의 입장을 거절했습니다.",
                    List.of(applicantName)
            );

        } catch (RuntimeException e) {
            log.error("개별 신청 거절 실패 - requestId: {}, 오류: {}", requestId, e.getMessage());
            return ActionResponse.failure(e.getMessage());

        } catch (Exception e) {
            log.error("예상치 못한 오류", e);
            return ActionResponse.failure("신청 처리 중 오류가 발생했습니다.");
        }
    }

    // 통계 데이터 조회
    public RequestStatsResponse getRequestStats() {
        log.info("신청 통계 데이터 조회 시작");

        try {
            Member roomOwner = securityUtil.getCurrentMember();
            List<Room> myRooms = roomRepository.findByCreatorAndIsDeleteRoomFalse(roomOwner);

            // 내 방들의 모든 신청 조회
            List<EnteringRoom> allRequests = myRooms.stream()
                    .flatMap(room -> joinRepository.findByRoom(room).stream())
                    .collect(Collectors.toList());

            // 통계 계산
            int totalRequests = (int) allRequests.stream()
                    .filter(req -> req.getStatus() == EnteringRoom.EnteringStatus.PENDING)
                    .count();

            int todayRequests = (int) allRequests.stream()
                    .filter(req -> req.getStatus() == EnteringRoom.EnteringStatus.PENDING)
                    .filter(req -> req.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                    .count();

            int urgentRequests = (int) allRequests.stream()
                    .filter(req -> req.getStatus() == EnteringRoom.EnteringStatus.PENDING)
                    .filter(this::isUrgentRequest)
                    .count();

            return RequestStatsResponse.of(totalRequests, todayRequests, urgentRequests, totalRequests);

        } catch (Exception e) {
            log.error("통계 데이터 조회 실패", e);
            return RequestStatsResponse.of(0, 0, 0, 0);
        }
    }

    private RoomWithRequestsResponse convertToRoomWithRequests(Room room) {
        List<EnteringRoom> pendingRequests = joinRepository.findByRoomIdWithProfile(room.getRoomId())
                .stream()
                .filter(er -> er.getStatus() == EnteringRoom.EnteringStatus.PENDING)
                .toList();

        List<JoinRequestListResponse> requestResponses = pendingRequests.stream()
                .map(JoinRequestListResponse::from)
                .toList();

        return RoomWithRequestsResponse.from(room, requestResponses);
    }

    private Room validateRoomOwnership(Long roomId) {
        Member currentMember = securityUtil.getCurrentMember();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));

        if (!room.getCreator().getMemberPk().equals(currentMember.getMemberPk())) {
            throw new RuntimeException("해당 방의 관리 권한이 없습니다.");
        }

        return room;
    }

    private EnteringRoom validateRequestOwnership(Long requestId) {
        EnteringRoom request = joinRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 신청입니다."));

        validateRoomOwnership(request.getRoom().getRoomId());
        return request;
    }

    private void addApprovedMemberToRoom(EnteringRoom request) {
        log.info("🏠 승인된 회원을 방에 추가 시작 - 방: {}, 신청자: {}",
                request.getRoom().getRoomName(), request.getApplicant().getNickname());

        try {
            // 1️⃣ RoomMember 테이블에 실제 참여자로 추가
            RoomMember newMember = RoomMember.builder()
                    .member(request.getApplicant())  // 신청자
                    .room(request.getRoom())         // 방
                    .joinedAt(LocalDateTime.now())   // 참여 시간
                    .role("MEMBER")                  // 일반 참여자
                    .isActive(true)                  // 활성 상태
                    .build();

            roomMemberRepository.save(newMember);
            log.info("✅ RoomMember 추가 완료");

            // 2️⃣ Room의 현재 인원 수 업데이트
            Room room = request.getRoom();

            // 실제 활성 참여자 수 다시 계산 (방장 포함)
            Long actualParticipantCount = roomMemberRepository.countByRoomAndIsActiveTrue(room);

            // 이전 인원 수 로깅
            int previousCount = room.getRoomCurrentCount();

            // Room 엔티티의 현재 인원 수 업데이트
            room.setRoomCurrentCount(actualParticipantCount.intValue());

            // Room 저장
            roomRepository.save(room);

            log.info("🔢 방 인원 수 업데이트 완료 - 방: {}, 이전: {}, 현재: {}/{}",
                    room.getRoomName(),
                    previousCount,
                    actualParticipantCount,
                    room.getRoomMaxCount());

        } catch (Exception e) {
            log.error("❌ 승인된 회원 추가 중 오류 발생", e);
            throw new RuntimeException("참여자 추가 중 오류가 발생했습니다.");
        }
    }

    private String processApproval(Long requestId) {
        try {
            ActionResponse result = approveRequest(requestId);
            return result.isSuccess() ? result.getProcessedNames().get(0) : null;
        } catch (Exception e) {
            log.warn("개별 승인 처리 실패 - requestId: {}", requestId);
            return null;
        }
    }

    private String processRejection(Long requestId) {
        try {
            ActionResponse result = rejectRequest(requestId);
            return result.isSuccess() ? result.getProcessedNames().get(0) : null;
        } catch (Exception e) {
            log.warn("개별 거절 처리 실패 - requestId: {}", requestId);
            return null;
        }
    }

    private boolean isUrgentRequest(EnteringRoom request) {
        // 2시간 이내 신청을 긴급 요청으로 분류
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        return request.getCreatedAt().isAfter(twoHoursAgo);
    }

    /**
     * 사이드바 알림 배지용 - 총 대기 요청 수만 간단히 조회
     * 다른 마이페이지들에서 사이드바에 배지 표시하기 위해 사용
     */
    public Integer getTotalPendingRequestsForSidebar() {
        log.debug("사이드바 배지용 총 대기 요청 수 조회");

        try {
            Member roomOwner = securityUtil.getCurrentMember();
            List<Room> myRooms = roomRepository.findByCreatorAndIsDeleteRoomFalse(roomOwner);

            // 각 방의 대기 중인 요청 수를 모두 합산
            int totalCount = 0;
            for (Room room : myRooms) {
                List<EnteringRoom> pendingRequests = joinRepository.findByRoomAndStatus(
                        room, EnteringRoom.EnteringStatus.PENDING);
                totalCount += pendingRequests.size();
            }

            log.debug("사이드바 배지용 총 대기 요청 수: {}", totalCount);
            return totalCount;

        } catch (Exception e) {
            log.error("사이드바용 대기 요청 수 조회 실패", e);
            return 0;  // 에러 시 0 반환
        }
    }
}