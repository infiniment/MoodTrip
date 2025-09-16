package com.moodTrip.spring.domain.fire.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.fire.dto.request.MemberFireRequest;
import com.moodTrip.spring.domain.fire.dto.response.MemberFireResponse;
import com.moodTrip.spring.domain.fire.service.MemberFireService;
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

@DisplayName("MemberFireApiController 테스트")
@ExtendWith(MockitoExtension.class)
class MemberFireApiControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MemberFireService memberFireService;

    @InjectMocks
    private MemberFireApiController controller;

    private MemberFireRequest validRequest;
    private MemberFireResponse successResponse;
    private MemberFireResponse failureResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // 테스트용 요청 데이터
        validRequest = MemberFireRequest.builder()
                .reportedNickname("신고당할유저")
                .reportReason("HARASSMENT")
                .reportMessage("부적절한 언행을 반복합니다")
                .build();

        // 성공 응답
        successResponse = MemberFireResponse.builder()
                .success(true)
                .message("신고가 접수되었습니다. 운영진이 확인 후 조치합니다.")
                .fireId(1L)
                .roomId(100L)
                .reportedNickname("신고당할유저")
                .firedAt(LocalDateTime.of(2025, 9, 4, 14, 30, 0))
                .fireReason("괴롭힘/혐오발언")
                .build();

        // 실패 응답
        failureResponse = MemberFireResponse.builder()
                .success(false)
                .message("신고 처리 중 오류가 발생했습니다.")
                .build();
    }

    @Test
    @DisplayName("멤버 신고 성공 테스트")
    void reportMember_Success() throws Exception {
        Long roomId = 100L;
        given(memberFireService.reportMember(eq(roomId), any(MemberFireRequest.class)))
                .willReturn(successResponse);

        mockMvc.perform(post("/api/v1/fires/rooms/{roomId}/members", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("신고가 접수되었습니다. 운영진이 확인 후 조치합니다."))
                .andExpect(jsonPath("$.fireId").value(1L))
                .andExpect(jsonPath("$.roomId").value(100L))
                .andExpect(jsonPath("$.reportedNickname").value("신고당할유저"))
                .andExpect(jsonPath("$.firedAt").exists())
                .andExpect(jsonPath("$.fireReason").value("괴롭힘/혐오발언"));

        verify(memberFireService, times(1)).reportMember(eq(roomId), any(MemberFireRequest.class));
    }

    @Test
    @DisplayName("신고 실패 시에도 200 OK와 실패 응답 반환")
    void reportMember_ServiceFailure_ReturnsOkWithFailureResponse() throws Exception {
        Long roomId = 100L;
        given(memberFireService.reportMember(eq(roomId), any(MemberFireRequest.class)))
                .willReturn(failureResponse);

        mockMvc.perform(post("/api/v1/fires/rooms/{roomId}/members", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("신고 처리 중 오류가 발생했습니다."));

        verify(memberFireService, times(1)).reportMember(eq(roomId), any(MemberFireRequest.class));
    }

    @Test
    @DisplayName("잘못된 JSON 형식으로 요청 시 400 에러")
    void reportMember_InvalidJson_BadRequest() throws Exception {
        Long roomId = 100L;
        String invalidJson = "{\"reportReason\": \"HARASSMENT\", \"reportMessage\": }";

        mockMvc.perform(post("/api/v1/fires/rooms/{roomId}/members", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("빈 문자열 신고 사유 테스트")
    void reportMember_BlankReportReason() throws Exception {
        Long roomId = 100L;
        MemberFireRequest requestWithBlankReason = MemberFireRequest.builder()
                .reportedNickname("테스트유저")
                .reportReason("   ")
                .reportMessage("테스트 메시지")
                .build();

        MemberFireResponse blankReasonFailure = MemberFireResponse.failure("신고 사유를 선택해주세요.");
        given(memberFireService.reportMember(eq(roomId), any(MemberFireRequest.class)))
                .willReturn(blankReasonFailure);

        mockMvc.perform(post("/api/v1/fires/rooms/{roomId}/members", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithBlankReason)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("신고 사유를 선택해주세요."));
    }

    @Test
    @DisplayName("신고 메시지가 1000자 초과 시 테스트")
    void reportMember_TooLongReportMessage() throws Exception {
        Long roomId = 100L;
        String tooLongMessage = "a".repeat(1001);

        MemberFireRequest requestWithTooLongMessage = MemberFireRequest.builder()
                .reportedNickname("테스트유저")
                .reportReason("OTHER")
                .reportMessage(tooLongMessage)
                .build();

        MemberFireResponse tooLongMessageFailure = MemberFireResponse.failure("신고 내용은 1000자 이내로 작성해주세요.");
        given(memberFireService.reportMember(eq(roomId), any(MemberFireRequest.class)))
                .willReturn(tooLongMessageFailure);

        mockMvc.perform(post("/api/v1/fires/rooms/{roomId}/members", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithTooLongMessage)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("신고 내용은 1000자 이내로 작성해주세요."));
    }

    @Test
    @DisplayName("Content-Type 헤더 없이 요청 시 415 에러")
    void reportMember_NoContentType_UnsupportedMediaType() throws Exception {
        Long roomId = 100L;

        mockMvc.perform(post("/api/v1/fires/rooms/{roomId}/members", roomId)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }
}
