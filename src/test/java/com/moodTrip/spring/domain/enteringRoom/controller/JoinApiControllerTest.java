// 📁 src/test/java/com/moodTrip/spring/domain/enteringRoom/controller/JoinApiControllerTest.java
package com.moodTrip.spring.domain.enteringRoom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.enteringRoom.dto.request.JoinRequest;
import com.moodTrip.spring.domain.enteringRoom.dto.response.JoinResponse;
import com.moodTrip.spring.domain.enteringRoom.service.JoinService;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JoinApiControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private JoinService joinService;

    @InjectMocks
    private JoinApiController joinApiController;

    private JoinRequest joinRequest;
    private JoinResponse successResponse;
    private JoinResponse failureResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(joinApiController).build();
        objectMapper = new ObjectMapper();

        joinRequest = JoinRequest.builder()
                .message("참여하고 싶습니다!") // JoinRequest 필드에 맞춰 수정
                .build();

        successResponse = JoinResponse.builder()
                .joinRequestId(1L)
                .resultMessage("신청이 완료되었습니다.")
                .success(true)
                .appliedAt(LocalDateTime.now())
                .build();

        failureResponse = JoinResponse.builder()
                .joinRequestId(null)
                .resultMessage("이미 신청한 방입니다.")
                .success(false)
                .appliedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("방 입장 신청 - 성공")
    void applyToRoom_Success() throws Exception {
        // given
        Long roomId = 100L;
        given(joinService.applyToRoom(eq(roomId), any(JoinRequest.class)))
                .willReturn(successResponse);

        // when & then
        mockMvc.perform(post("/api/v1/companion-rooms/{room_id}/join-requests", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.resultMessage").value("신청이 완료되었습니다."))
                .andExpect(jsonPath("$.joinRequestId").value(1L));
    }

    @Test
    @DisplayName("방 입장 신청 - 비즈니스 실패 (예: 이미 신청함)")
    void applyToRoom_Failure() throws Exception {
        // given
        Long roomId = 100L;
        given(joinService.applyToRoom(eq(roomId), any(JoinRequest.class)))
                .willReturn(failureResponse);

        // when & then
        mockMvc.perform(post("/api/v1/companion-rooms/{room_id}/join-requests", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andDo(print())
                .andExpect(status().isOk()) // 실패여도 200 OK
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.resultMessage").value("이미 신청한 방입니다."))
                .andExpect(jsonPath("$.joinRequestId").doesNotExist());
    }

    @Test
    @DisplayName("방 입장 신청 - 시스템 오류 발생 시 500 반환")
    void applyToRoom_Exception() throws Exception {
        // given
        Long roomId = 100L;
        doThrow(new RuntimeException("DB 오류")).when(joinService).applyToRoom(eq(roomId), any(JoinRequest.class));

        // when & then
        mockMvc.perform(post("/api/v1/companion-rooms/{room_id}/join-requests", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.resultMessage").value("시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}
