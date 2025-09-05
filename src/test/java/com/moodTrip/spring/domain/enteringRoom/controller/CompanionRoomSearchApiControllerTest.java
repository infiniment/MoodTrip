package com.moodTrip.spring.domain.enteringRoom.controller;

import com.moodTrip.spring.domain.enteringRoom.dto.response.CompanionRoomListResponse;
import com.moodTrip.spring.domain.enteringRoom.service.CompanionRoomService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CompanionRoomSearchApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CompanionRoomService companionRoomService;

    @InjectMocks
    private CompanionRoomSearchApiController companionRoomSearchApiController;

    private List<CompanionRoomListResponse> testRooms;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(companionRoomSearchApiController).build();

        // ✅ Builder 기반으로 테스트 데이터 생성
        testRooms = Arrays.asList(
                CompanionRoomListResponse.builder()
                        .id(1L)
                        .title("서울 여행 방")
                        .location("서울")
                        .maxParticipants(4)
                        .urgent(false)
                        .build(),
                CompanionRoomListResponse.builder()
                        .id(2L)
                        .title("부산 바다 방")
                        .location("부산")
                        .maxParticipants(2)
                        .urgent(true)
                        .build()
        );
    }

    @Test
    @DisplayName("방 목록 조회 - 전체 조회 성공")
    void getAllRooms_Success() throws Exception {
        // given
        when(companionRoomService.getAllRooms()).thenReturn(testRooms);

        // when & then
        mockMvc.perform(get("/api/v1/companion-rooms/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("서울 여행 방"))
                .andExpect(jsonPath("$[1].title").value("부산 바다 방"));
    }

    @Test
    @DisplayName("방 목록 조회 - 검색 키워드로 필터링 성공")
    void searchRooms_Success() throws Exception {
        // given
        when(companionRoomService.searchRooms(anyString())).thenReturn(List.of(testRooms.get(0)));

        // when & then
        mockMvc.perform(get("/api/v1/companion-rooms/search")
                        .param("search", "서울")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("서울 여행 방"));
    }

    @Test
    @DisplayName("방 상세보기 - 존재하는 방")
    void getRoomDetail_Success() throws Exception {
        // given
        when(companionRoomService.getAllRooms()).thenReturn(testRooms);

        // when & then
        mockMvc.perform(get("/api/v1/companion-rooms/search/{room_id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("서울 여행 방"));
    }

    @Test
    @DisplayName("방 상세보기 - 존재하지 않는 방")
    void getRoomDetail_NotFound() throws Exception {
        // given
        when(companionRoomService.getAllRooms()).thenReturn(testRooms);

        // when & then
        mockMvc.perform(get("/api/v1/companion-rooms/search/{room_id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
