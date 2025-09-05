package com.moodTrip.spring.domain.mypageRoom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.mypageRoom.dto.response.CreatedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.dto.response.JoinedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.service.MypageRoomService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.assertThrows;
import jakarta.servlet.ServletException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MypageRoomApiControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MypageRoomService mypageRoomService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private MypageRoomApiController mypageRoomApiController;

    private Member testMember;
    private List<JoinedRoomResponse> joinedRoomResponses;
    private List<CreatedRoomResponse> createdRoomResponses;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mypageRoomApiController).build();

        // 테스트용 Member 객체 생성
        testMember = Member.builder()
                .memberPk(1L)
                .memberId("testUser")
                .nickname("테스트유저")
                .memberPhone("010-1234-5678")
                .email("test@example.com")
                .memberAuth("U")
                .isWithdraw(false)
                .build();

        // 테스트용 JoinedRoomResponse 리스트 생성
        joinedRoomResponses = Arrays.asList(
                JoinedRoomResponse.builder()
                        .roomId(1L)
                        .roomName("서울 여행 방")
                        .roomDescription("서울 맛집 투어")
                        .maxCount(4)
                        .currentCount(3)
                        .travelStartDate(LocalDate.now().plusDays(7))
                        .travelEndDate(LocalDate.now().plusDays(10))
                        .destinationCategory("관광지")
                        .destinationName("서울")
                        .creatorNickname("방장")
                        .myRole("MEMBER")
                        .joinedAt(LocalDateTime.now().minusDays(1))
                        .roomCreatedAt(LocalDateTime.now().minusDays(5))
                        .emotions(Arrays.asList("즐거운", "활기찬"))
                        .image("/image/attraction/seoul.jpg")
                        .build(),
                JoinedRoomResponse.builder()
                        .roomId(2L)
                        .roomName("부산 여행 방")
                        .roomDescription("부산 바다 여행")
                        .maxCount(6)
                        .currentCount(4)
                        .travelStartDate(LocalDate.now().plusDays(14))
                        .travelEndDate(LocalDate.now().plusDays(17))
                        .destinationCategory("관광지")
                        .destinationName("부산")
                        .creatorNickname("부산방장")
                        .myRole("MEMBER")
                        .joinedAt(LocalDateTime.now().minusDays(3))
                        .roomCreatedAt(LocalDateTime.now().minusDays(7))
                        .emotions(Arrays.asList("편안한", "낭만적인"))
                        .image("/image/attraction/busan.jpg")
                        .build()
        );

        // 테스트용 CreatedRoomResponse 리스트 생성
        createdRoomResponses = Arrays.asList(
                CreatedRoomResponse.builder()
                        .roomId(3L)
                        .roomName("제주도 여행 방")
                        .roomDescription("제주도 힐링 여행")
                        .maxCount(5)
                        .currentCount(2)
                        .travelStartDate(LocalDate.now().plusDays(21))
                        .travelEndDate(LocalDate.now().plusDays(25))
                        .destinationCategory("관광지")
                        .destinationName("제주도")
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .emotions(Arrays.asList("평온한", "자연친화적인"))
                        .image("/image/attraction/jeju.jpg")
                        .build(),
                CreatedRoomResponse.builder()
                        .roomId(4L)
                        .roomName("경주 여행 방")
                        .roomDescription("경주 역사 탐방")
                        .maxCount(4)
                        .currentCount(1)
                        .travelStartDate(LocalDate.now().plusDays(28))
                        .travelEndDate(LocalDate.now().plusDays(30))
                        .destinationCategory("관광지")
                        .destinationName("경주")
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .emotions(Arrays.asList("신비로운", "역사적인"))
                        .image("/image/attraction/gyeongju.jpg")
                        .build()
        );
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("내가 참여한 방 목록 조회 - 성공")
    void getMyJoinedRooms_Success() throws Exception {
        // Given
        when(securityUtil.getCurrentMember()).thenReturn(testMember);
        when(mypageRoomService.getMyJoinedRooms(testMember)).thenReturn(joinedRoomResponses);

        // When & Then
        mockMvc.perform(get("/api/v1/mypage/rooms/joined")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roomId").value(1))
                .andExpect(jsonPath("$[0].roomName").value("서울 여행 방"))
                .andExpect(jsonPath("$[0].roomDescription").value("서울 맛집 투어"))
                .andExpect(jsonPath("$[0].maxCount").value(4))
                .andExpect(jsonPath("$[0].currentCount").value(3))
                .andExpect(jsonPath("$[0].destinationCategory").value("관광지"))
                .andExpect(jsonPath("$[0].destinationName").value("서울"))
                .andExpect(jsonPath("$[0].creatorNickname").value("방장"))
                .andExpect(jsonPath("$[0].myRole").value("MEMBER"))
                .andExpect(jsonPath("$[0].emotions").isArray())
                .andExpect(jsonPath("$[0].emotions[0]").value("즐거운"))
                .andExpect(jsonPath("$[0].emotions[1]").value("활기찬"))
                .andExpect(jsonPath("$[0].image").value("/image/attraction/seoul.jpg"))
                .andExpect(jsonPath("$[1].roomId").value(2))
                .andExpect(jsonPath("$[1].roomName").value("부산 여행 방"));

        verify(securityUtil, times(1)).getCurrentMember();
        verify(mypageRoomService, times(1)).getMyJoinedRooms(testMember);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("내가 만든 방 목록 조회 - 성공")
    void getMyCreatedRooms_Success() throws Exception {
        // Given
        when(securityUtil.getCurrentMember()).thenReturn(testMember);
        when(mypageRoomService.getMyCreatedRooms(testMember)).thenReturn(createdRoomResponses);

        // When & Then
        mockMvc.perform(get("/api/v1/mypage/rooms/created")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roomId").value(3))
                .andExpect(jsonPath("$[0].roomName").value("제주도 여행 방"))
                .andExpect(jsonPath("$[0].roomDescription").value("제주도 힐링 여행"))
                .andExpect(jsonPath("$[0].maxCount").value(5))
                .andExpect(jsonPath("$[0].currentCount").value(2))
                .andExpect(jsonPath("$[0].destinationCategory").value("관광지"))
                .andExpect(jsonPath("$[0].destinationName").value("제주도"))
                .andExpect(jsonPath("$[0].emotions").isArray())
                .andExpect(jsonPath("$[0].emotions[0]").value("평온한"))
                .andExpect(jsonPath("$[0].emotions[1]").value("자연친화적인"))
                .andExpect(jsonPath("$[0].image").value("/image/attraction/jeju.jpg"))
                .andExpect(jsonPath("$[1].roomId").value(4))
                .andExpect(jsonPath("$[1].roomName").value("경주 여행 방"));

        verify(securityUtil, times(1)).getCurrentMember();
        verify(mypageRoomService, times(1)).getMyCreatedRooms(testMember);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("방 삭제 - 성공")
    void deleteRoom_Success() throws Exception {
        // Given
        Long roomId = 1L;
        when(securityUtil.getCurrentMember()).thenReturn(testMember);
        doNothing().when(mypageRoomService).deleteRoom(roomId, testMember);

        // When & Then
        mockMvc.perform(delete("/api/v1/mypage/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(securityUtil, times(1)).getCurrentMember();
        verify(mypageRoomService, times(1)).deleteRoom(roomId, testMember);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("방 삭제 - 권한 없음 예외")
    void deleteRoom_AccessDenied() throws Exception {
        // Given
        Long roomId = 1L;
        when(securityUtil.getCurrentMember()).thenReturn(testMember);
        doThrow(new AccessDeniedException("방을 삭제할 권한이 없습니다."))
                .when(mypageRoomService).deleteRoom(roomId, testMember);

        // When & Then - AccessDeniedException이 직접 던져짐
        assertThrows(AccessDeniedException.class, () -> {
            mockMvc.perform(delete("/api/v1/mypage/rooms/{roomId}", roomId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());
        });

        verify(securityUtil, times(1)).getCurrentMember();
        verify(mypageRoomService, times(1)).deleteRoom(roomId, testMember);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("방 나가기 - 성공")
    void leaveRoom_Success() throws Exception {
        // Given
        Long roomId = 1L;
        when(securityUtil.getCurrentMember()).thenReturn(testMember);
        doNothing().when(mypageRoomService).leaveRoom(roomId, testMember);

        // When & Then
        mockMvc.perform(delete("/api/v1/mypage/rooms/{roomId}/leave", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(securityUtil, times(1)).getCurrentMember();
        verify(mypageRoomService, times(1)).leaveRoom(roomId, testMember);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("내가 참여한 방 목록 조회 - 빈 리스트")
    void getMyJoinedRooms_EmptyList() throws Exception {
        // Given
        when(securityUtil.getCurrentMember()).thenReturn(testMember);
        when(mypageRoomService.getMyJoinedRooms(testMember)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/mypage/rooms/joined")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(securityUtil, times(1)).getCurrentMember();
        verify(mypageRoomService, times(1)).getMyJoinedRooms(testMember);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("내가 만든 방 목록 조회 - 빈 리스트")
    void getMyCreatedRooms_EmptyList() throws Exception {
        // Given
        when(securityUtil.getCurrentMember()).thenReturn(testMember);
        when(mypageRoomService.getMyCreatedRooms(testMember)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/mypage/rooms/created")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(securityUtil, times(1)).getCurrentMember();
        verify(mypageRoomService, times(1)).getMyCreatedRooms(testMember);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("존재하지 않는 방 삭제 시도")
    void deleteRoom_RoomNotFound() throws Exception {
        // Given
        Long nonExistentRoomId = 999L;
        when(securityUtil.getCurrentMember()).thenReturn(testMember);
        doThrow(new IllegalArgumentException("존재하지 않는 방입니다."))
                .when(mypageRoomService).deleteRoom(nonExistentRoomId, testMember);

        // When & Then - ServletException으로 감싸져서 나옴
        assertThrows(ServletException.class, () -> {
            mockMvc.perform(delete("/api/v1/mypage/rooms/{roomId}", nonExistentRoomId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());
        });

        verify(securityUtil, times(1)).getCurrentMember();
        verify(mypageRoomService, times(1)).deleteRoom(nonExistentRoomId, testMember);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    @DisplayName("존재하지 않는 방에서 나가기 시도")
    void leaveRoom_RoomNotFound() throws Exception {
        // Given
        Long nonExistentRoomId = 999L;
        when(securityUtil.getCurrentMember()).thenReturn(testMember);
        doThrow(new IllegalArgumentException("존재하지 않는 방입니다."))
                .when(mypageRoomService).leaveRoom(nonExistentRoomId, testMember);

        // When & Then - ServletException으로 감싸져서 나옴
        assertThrows(ServletException.class, () -> {
            mockMvc.perform(delete("/api/v1/mypage/rooms/{roomId}/leave", nonExistentRoomId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());
        });

        verify(securityUtil, times(1)).getCurrentMember();
        verify(mypageRoomService, times(1)).leaveRoom(nonExistentRoomId, testMember);
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 접근 - SecurityUtil에서 인증 확인")
    void unauthorizedAccess() throws Exception {
        // Given - 인증되지 않은 상태에서는 SecurityUtil.getCurrentMember()가 예외를 던진다고 가정
        when(securityUtil.getCurrentMember()).thenThrow(new RuntimeException("인증되지 않은 사용자"));

        // When & Then - ServletException으로 감싸져서 나올 가능성
        assertThrows(ServletException.class, () -> {
            mockMvc.perform(get("/api/v1/mypage/rooms/joined")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print());
        });

        verify(securityUtil, times(1)).getCurrentMember();
        verify(mypageRoomService, never()).getMyJoinedRooms(any());
    }
}