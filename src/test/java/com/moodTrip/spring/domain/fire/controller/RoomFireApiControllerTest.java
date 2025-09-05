package com.moodTrip.spring.domain.fire.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.fire.dto.request.RoomFireRequest;
import com.moodTrip.spring.domain.fire.dto.response.RoomFireResponse;
import com.moodTrip.spring.domain.fire.service.RoomFireService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoomFireApiControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RoomFireService roomFireService;

    @InjectMocks
    private RoomFireApiController roomFireApiController;

    private RoomFireRequest validRequest;
    private RoomFireResponse successResponse;
    private RoomFireResponse failureResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roomFireApiController).build();

        validRequest = RoomFireRequest.builder()
                .reportReason("spam")
                .reportMessage("광고 도배가 심합니다")
                .build();

        successResponse = RoomFireResponse.builder()
                .success(true)
                .message("신고가 정상적으로 접수되었습니다. 검토 후 적절한 조치를 취하겠습니다.")
                .fireId(1L)
                .roomId(100L)
                .roomTitle("테스트 방")
                .firedAt(LocalDateTime.now())
                .fireReason("스팸/광고")
                .build();

        failureResponse = RoomFireResponse.failure("신고 처리 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("방 신고 성공")
    void reportRoom_Success() throws Exception {
        Long roomId = 100L;
        given(roomFireService.fireRoom(eq(roomId), any(RoomFireRequest.class)))
                .willReturn(successResponse);

        mockMvc.perform(post("/api/v1/fires/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("신고가 정상적으로 접수되었습니다. 검토 후 적절한 조치를 취하겠습니다."))
                .andExpect(jsonPath("$.fireId").value(1L))
                .andExpect(jsonPath("$.roomId").value(100L))
                .andExpect(jsonPath("$.roomTitle").value("테스트 방"))
                .andExpect(jsonPath("$.fireReason").value("스팸/광고"))
                .andExpect(jsonPath("$.firedAt").exists());

        verify(roomFireService, times(1)).fireRoom(eq(roomId), any(RoomFireRequest.class));
    }

    @Test
    @DisplayName("방 신고 실패 (서비스 실패 응답)")
    void reportRoom_Failure() throws Exception {
        Long roomId = 100L;
        given(roomFireService.fireRoom(eq(roomId), any(RoomFireRequest.class)))
                .willReturn(failureResponse);

        mockMvc.perform(post("/api/v1/fires/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("신고 처리 중 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("잘못된 JSON 요청 → 400 BadRequest")
    void reportRoom_InvalidJson() throws Exception {
        Long roomId = 100L;
        String invalidJson = "{\"reportReason\": \"spam\", \"reportMessage\": }";

        mockMvc.perform(post("/api/v1/fires/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Content-Type 헤더 누락 → 415 UnsupportedMediaType")
    void reportRoom_NoContentType() throws Exception {
        Long roomId = 100L;

        mockMvc.perform(post("/api/v1/fires/rooms/{roomId}", roomId)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("서비스에서 IllegalArgumentException 발생 시 400 반환")
    void reportRoom_ServiceThrowsIllegalArgumentException() throws Exception {
        Long roomId = 100L;
        given(roomFireService.fireRoom(eq(roomId), any(RoomFireRequest.class)))
                .willThrow(new IllegalArgumentException("유효하지 않은 신고 사유입니다."));

        mockMvc.perform(post("/api/v1/fires/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 신고 사유입니다."));
    }
}
