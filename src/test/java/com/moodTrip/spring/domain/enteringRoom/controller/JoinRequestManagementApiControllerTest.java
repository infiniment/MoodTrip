package com.moodTrip.spring.domain.enteringRoom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.enteringRoom.dto.response.ActionResponse;
import com.moodTrip.spring.domain.enteringRoom.dto.response.JoinRequestListResponse;
import com.moodTrip.spring.domain.enteringRoom.dto.response.RequestStatsResponse;
import com.moodTrip.spring.domain.enteringRoom.dto.response.RoomWithRequestsResponse;
import com.moodTrip.spring.domain.enteringRoom.service.JoinRequestManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JoinRequestManagementApiControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JoinRequestManagementService joinRequestManagementService;

    @InjectMocks
    private JoinRequestManagementApiController joinRequestManagementApiController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(joinRequestManagementApiController).build();
    }

    @Test
    @DisplayName("방장의 방 목록 + 신청 목록 조회 성공")
    void getMyRoomsWithRequests_Success() throws Exception {
        // given
        RoomWithRequestsResponse room = RoomWithRequestsResponse.builder()
                .roomId(1L)
                .roomTitle("테스트 방")
                .travelDate("25/09/04")
                .currentParticipants(2)
                .maxParticipants(5)
                .pendingRequestsCount(0)
                .joinRequests(List.of())
                .build();

        when(joinRequestManagementService.getMyRoomsWithRequests()).thenReturn(List.of(room));

        // when & then
        mockMvc.perform(get("/api/v1/join-requests/rooms"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomId").value(1L))
                .andExpect(jsonPath("$[0].roomTitle").value("테스트 방"))
                .andExpect(jsonPath("$[0].travelDate").value("25/09/04"))
                .andExpect(jsonPath("$[0].currentParticipants").value(2))
                .andExpect(jsonPath("$[0].maxParticipants").value(5))
                .andExpect(jsonPath("$[0].pendingRequestsCount").value(0));

        verify(joinRequestManagementService, times(1)).getMyRoomsWithRequests();
    }

    @Test
    @DisplayName("특정 방 신청 목록 조회 성공")
    void getRoomRequests_Success() throws Exception {
        // given
        JoinRequestListResponse request = JoinRequestListResponse.builder()
                .joinRequestId(10L)
                .applicantNickname("신청자")
                .status("PENDING")
                .isVerified(true)
                .hasPhoneVerified(false)
                .message("테스트 메시지")
                .appliedAt("2025-09-04 12:00")
                .timeAgo("2시간 전")
                .priority("HIGH")
                .applicantProfileImage("/image/test.jpg")
                .build();

        when(joinRequestManagementService.getRoomRequests(1L)).thenReturn(List.of(request));

        // when & then
        mockMvc.perform(get("/api/v1/join-requests/rooms/{roomId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].joinRequestId").value(10L))
                .andExpect(jsonPath("$[0].applicantNickname").value("신청자"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].verified").value(true))          // ✅ 필드명 수정
                .andExpect(jsonPath("$[0].hasPhoneVerified").value(false))
                .andExpect(jsonPath("$[0].priority").value("HIGH"));

        verify(joinRequestManagementService, times(1)).getRoomRequests(1L);
    }

    @Test
    @DisplayName("개별 신청 승인 성공")
    void approveRequest_Success() throws Exception {
        // given
        ActionResponse response = ActionResponse.success("승인 완료", List.of("신청자"));
        when(joinRequestManagementService.approveRequest(1L)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/join-requests/{requestId}/approve", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("승인 완료"))
                .andExpect(jsonPath("$.totalProcessed").value(1));

        verify(joinRequestManagementService, times(1)).approveRequest(1L);
    }

    @Test
    @DisplayName("개별 신청 거절 성공")
    void rejectRequest_Success() throws Exception {
        // given
        ActionResponse response = ActionResponse.success("거절 완료", List.of("신청자"));
        when(joinRequestManagementService.rejectRequest(1L)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/join-requests/{requestId}/reject", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("거절 완료"))
                .andExpect(jsonPath("$.totalProcessed").value(1));

        verify(joinRequestManagementService, times(1)).rejectRequest(1L);
    }

    @Test
    @DisplayName("통계 데이터 조회 성공")
    void getRequestStats_Success() throws Exception {
        // given
        RequestStatsResponse stats = RequestStatsResponse.of(5, 3, 1, 1); // 총 5건, 오늘 3건, 긴급 1건, 대기 1건
        when(joinRequestManagementService.getRequestStats()).thenReturn(stats);

        // when & then
        mockMvc.perform(get("/api/v1/join-requests/stats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").value(5))
                .andExpect(jsonPath("$.todayRequests").value(3))   // ✅ 필드명 맞춤
                .andExpect(jsonPath("$.urgentRequests").value(1))
                .andExpect(jsonPath("$.pendingRequests").value(1));

        verify(joinRequestManagementService, times(1)).getRequestStats();
    }

}
