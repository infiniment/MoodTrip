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
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JoinRequestManagementServiceTest {

    @Mock private JoinRepository joinRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomMemberRepository roomMemberRepository;
    @Mock private SecurityUtil securityUtil;
    @Mock private NotificationDataService notificationService;

    @InjectMocks
    private JoinRequestManagementService service;

    private Member roomOwner;
    private Room testRoom;
    private EnteringRoom pendingRequest;

    @BeforeEach
    void setUp() {
        roomOwner = Member.builder()
                .memberPk(1L)
                .nickname("방장")
                .build();

        testRoom = Room.builder()
                .roomId(100L)
                .roomName("테스트 방")
                .creator(roomOwner)
                .roomMaxCount(5)
                .roomCurrentCount(1)
                .isDeleteRoom(false)
                .build();

        pendingRequest = EnteringRoom.builder()
                .enteringRoomId(10L)
                .room(testRoom)
                .applicant(Member.builder().memberPk(2L).nickname("신청자").build())
                .status(EnteringRoom.EnteringStatus.PENDING)
                .message("입장 신청합니다")
                .build();
        ReflectionTestUtils.setField(pendingRequest, "createdAt", LocalDateTime.now().minusMinutes(30));
    }

    @Test
    @DisplayName("방장의 방 목록 + 신청 목록 조회 성공")
    void getMyRoomsWithRequests_Success() {
        when(securityUtil.getCurrentMember()).thenReturn(roomOwner);
        when(roomRepository.findByCreatorAndIsDeleteRoomFalse(roomOwner)).thenReturn(List.of(testRoom));
        when(joinRepository.findByRoomIdWithProfile(testRoom.getRoomId())).thenReturn(List.of(pendingRequest));

        List<RoomWithRequestsResponse> result = service.getMyRoomsWithRequests();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomTitle()).isEqualTo("테스트 방");
        assertThat(result.get(0).getJoinRequests()).hasSize(1);
    }

    @Test
    @DisplayName("특정 방의 신청 목록 조회 성공")
    void getRoomRequests_Success() {
        when(securityUtil.getCurrentMember()).thenReturn(roomOwner);
        when(roomRepository.findById(100L)).thenReturn(Optional.of(testRoom));
        when(joinRepository.findByRoomIdWithProfile(100L)).thenReturn(List.of(pendingRequest));

        List<JoinRequestListResponse> result = service.getRoomRequests(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApplicantNickname()).isEqualTo("신청자");
    }

    @Test
    @DisplayName("개별 신청 승인 성공")
    void approveRequest_Success() {
        when(joinRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(securityUtil.getCurrentMember()).thenReturn(roomOwner);
        when(roomRepository.findById(100L)).thenReturn(Optional.of(testRoom));
        when(joinRepository.countApprovedByRoom(testRoom)).thenReturn(1L);
        when(roomMemberRepository.countByRoomAndIsActiveTrue(testRoom)).thenReturn(2L);

        ActionResponse response = service.approveRequest(10L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProcessedNames()).contains("신청자");
        verify(notificationService, times(1)).saveNotification(any(), any());
    }

    @Test
    @DisplayName("개별 신청 거절 성공")
    void rejectRequest_Success() {
        when(joinRepository.findById(10L)).thenReturn(Optional.of(pendingRequest));
        when(securityUtil.getCurrentMember()).thenReturn(roomOwner);
        when(roomRepository.findById(100L)).thenReturn(Optional.of(testRoom));

        ActionResponse response = service.rejectRequest(10L);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getProcessedNames()).contains("신청자");
        verify(notificationService, times(1)).saveNotification(any(), any());
    }

    @Test
    @DisplayName("신청 통계 데이터 조회 성공")
    void getRequestStats_Success() {
        when(securityUtil.getCurrentMember()).thenReturn(roomOwner);
        when(roomRepository.findByCreatorAndIsDeleteRoomFalse(roomOwner)).thenReturn(List.of(testRoom));
        when(joinRepository.findByRoom(testRoom)).thenReturn(List.of(pendingRequest));

        RequestStatsResponse stats = service.getRequestStats();

        assertThat(stats.getTotalRequests()).isEqualTo(1);
        assertThat(stats.getTodayRequests()).isEqualTo(1);
        assertThat(stats.getUrgentRequests()).isEqualTo(1);
        assertThat(stats.getPendingRequests()).isEqualTo(1);
    }

    @Test
    @DisplayName("사이드바 대기 요청 수 조회 성공")
    void getTotalPendingRequestsForSidebar_Success() {
        when(securityUtil.getCurrentMember()).thenReturn(roomOwner);
        when(roomRepository.findByCreatorAndIsDeleteRoomFalse(roomOwner)).thenReturn(List.of(testRoom));
        when(joinRepository.findByRoomAndStatus(testRoom, EnteringRoom.EnteringStatus.PENDING))
                .thenReturn(List.of(pendingRequest));

        int result = service.getTotalPendingRequestsForSidebar();

        assertThat(result).isEqualTo(1);
    }
}
