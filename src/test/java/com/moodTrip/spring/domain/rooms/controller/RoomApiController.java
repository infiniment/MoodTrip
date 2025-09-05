package com.moodTrip.spring.domain.rooms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest;
import com.moodTrip.spring.domain.rooms.dto.request.UpdateRoomRequest;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.global.common.util.SecurityUtil; // ⬅️ 추가
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // ⬅️ 이거 사용
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoomApiController.class)
@AutoConfigureMockMvc(addFilters = false)   // 시큐리티 필터 비활성화
class RoomApiControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    @MockitoBean
    RoomService roomService;   // 컨트롤러 의존성
    @MockitoBean SecurityUtil securityUtil; // Advice가 요구하는 유틸 빈 목 처리

    @MockitoBean
    EmotionService emotionService;

    private void setAuthInSecurityContext(MyUserDetails principal) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    // 테스트 클래스 내부에 헬퍼 추가 (필요 시 import, 예: java.time.LocalDateTime)
    private String validCreateRequestJson() throws Exception {
        // 날짜 범위 1개 생성
        var start = OffsetDateTime.now().plusDays(7);
        var end   = OffsetDateTime.now().plusDays(9);

        var range = RoomRequest.ScheduleDto.DateRangeDto.builder()
                .startDate(start)
                .endDate(end)
                .startDateFormatted(start.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .endDateFormatted(end.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build();

        var schedule = RoomRequest.ScheduleDto.builder()
                .dateRanges(List.of(range))
                .totalDays(3)
                .rangeCount(1)
                .build();

        var emotion = RoomRequest.EmotionDto.builder()
                .tagId(1L)
                .text("평온")
                .build();

        var req = RoomRequest.builder()
                .attractionId(123L)
                .emotions(List.of(emotion))
                .schedule(schedule)
                .maxParticipants(4)
                .roomName("제주 한라산 동행")
                .roomDescription("10/2~10/3 한라산 성판악 코스")
                .version("v1")
                .build();

        return om.writeValueAsString(req);
    }

    private RequestPostProcessor auth() {
        return SecurityMockMvcRequestPostProcessors.user(myUserDetails());
    }

    private MyUserDetails myUserDetails() {
        Member member = Mockito.mock(Member.class);
        when(member.getMemberPk()).thenReturn(100L);
        MyUserDetails mud = Mockito.mock(MyUserDetails.class);
        when(mud.getMember()).thenReturn(member);
        when(mud.getUsername()).thenReturn("tester");
        return mud;
    }

    @Test
    @DisplayName("POST /api/v1/companion-rooms - 방 생성 201")
    void createRoom_201() throws Exception {
        String requestJson = validCreateRequestJson();

        RoomResponse stubResponse = Mockito.mock(RoomResponse.class);
        when(roomService.createRoom(any(RoomRequest.class), eq(100L))).thenReturn(stubResponse);

        // ⬇️ 여기 핵심: SecurityContextHolder에 직접 주입
        var principal = myUserDetails(); // memberPk=100L로 스텁된 것
        setAuthInSecurityContext(principal);
        try {
            mockMvc.perform(
                            post("/api/v1/companion-rooms")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content(requestJson)
                    )
                    .andExpect(status().isCreated());
        } finally {
            SecurityContextHolder.clearContext(); // 깨끗이 정리
        }
    }

    @Test
    @DisplayName("GET /api/v1/companion-rooms - 전체 조회 200")
    void getAllRooms_200() throws Exception {
        RoomResponse r1 = Mockito.mock(RoomResponse.class);
        RoomResponse r2 = Mockito.mock(RoomResponse.class);
        when(roomService.getAllRooms()).thenReturn(List.of(r1, r2));

        mockMvc.perform(
                        get("/api/v1/companion-rooms")
                                .with(auth())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/v1/companion-rooms/{id} - 단건 조회 200")
    void getRoom_200() throws Exception {
        RoomResponse r = Mockito.mock(RoomResponse.class);
        when(roomService.getRoomById(1L)).thenReturn(r);

        mockMvc.perform(
                        get("/api/v1/companion-rooms/{id}", 1L)
                                .with(auth())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/companion-rooms/{id} - 존재하지 않을 때 404")
    void getRoom_404() throws Exception {
        when(roomService.getRoomById(999L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "not found"));

        mockMvc.perform(
                        get("/api/v1/companion-rooms/{id}", 999L)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/companion-rooms/{id} - 수정 200")
    void updateRoom_200() throws Exception {
        String requestJson = """
        {
          "title": "한라산 당일치기",
          "maxParticipants": 3
        }
        """;

        RoomResponse r = Mockito.mock(RoomResponse.class);
        when(roomService.updateRoom(eq(1L), any(UpdateRoomRequest.class))).thenReturn(r);

        mockMvc.perform(
                        patch("/api/v1/companion-rooms/{id}", 1L)
                                .with(auth())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/companion-rooms/{id} - 존재하지 않을 때 404")
    void updateRoom_404() throws Exception {
        String requestJson = """
    { "title": "수정 타이틀" }
    """;

        when(roomService.updateRoom(eq(999L), any(UpdateRoomRequest.class)))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "not found"));

        mockMvc.perform(
                        patch("/api/v1/companion-rooms/{id}", 999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/companion-rooms/{id} - 삭제 204")
    void deleteRoom_204() throws Exception {
        doNothing().when(roomService).deleteRoomById(1L);

        mockMvc.perform(
                        delete("/api/v1/companion-rooms/{id}", 1L)
                                .with(auth())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/companion-rooms/{id} - 존재하지 않을 때 404")
    void deleteRoom_404() throws Exception {
        Mockito.doThrow(new ResponseStatusException(NOT_FOUND, "not found"))
                .when(roomService).deleteRoomById(999L);

        mockMvc.perform(
                        delete("/api/v1/companion-rooms/{id}", 999L)
                                .with(auth())
                )
                .andExpect(status().isNotFound());
    }


}
