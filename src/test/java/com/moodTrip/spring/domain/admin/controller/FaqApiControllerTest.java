package com.moodTrip.spring.domain.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.admin.entity.Faq;
import com.moodTrip.spring.domain.admin.service.FaqService;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FaqApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FaqApiController 단위 테스트")
class FaqApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ✅ ControllerAdvice가 필요로 하는 서비스들을 목으로 채워 넣기
    @MockBean
    private EmotionService emotionService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FaqService faqService;

    @MockBean
    private SecurityUtil securityUtil;

    private Faq testFaq1;
    private Faq testFaq2;
    private List<Faq> testFaqList;

    @BeforeEach
    void setUp() {
        testFaq1 = createTestFaq(1L, "결제 관련 질문", "결제는 어떻게 하나요?", "결제");
        testFaq2 = createTestFaq(2L, "예약 취소 방법", "예약을 취소하려면 어떻게 해야 하나요?", "예약");
        testFaqList = Arrays.asList(testFaq1, testFaq2);
    }

    private Faq createTestFaq(Long id, String title, String content, String category) {
        Faq faq = new Faq();
        faq.setId(id);
        faq.setTitle(title);
        faq.setContent(content);
        faq.setCategory(category);
        faq.setCreatedAt(LocalDateTime.now());
        faq.setModifiedAt(LocalDateTime.now());
        return faq;
    }

    @Test
    @DisplayName("모든 FAQ 조회 - 성공")
    void getAllFaqs_Success() throws Exception {
        // Given
        given(faqService.findAll()).willReturn(testFaqList);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/faq"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("결제 관련 질문"))
                .andExpect(jsonPath("$[0].content").value("결제는 어떻게 하나요?"))
                .andExpect(jsonPath("$[0].category").value("결제"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("예약 취소 방법"))
                .andExpect(jsonPath("$[1].content").value("예약을 취소하려면 어떻게 해야 하나요?"))
                .andExpect(jsonPath("$[1].category").value("예약"));

        verify(faqService, times(1)).findAll();
    }

    @Test
    @DisplayName("모든 FAQ 조회 - 빈 목록")
    void getAllFaqs_EmptyList() throws Exception {
        // Given
        given(faqService.findAll()).willReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/admin/faq"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(faqService, times(1)).findAll();
    }

    @Test
    @DisplayName("FAQ 생성 - 성공")
    void createFaq_Success() throws Exception {
        // Given
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "새로운 FAQ");
        requestData.put("content", "새로운 FAQ 내용입니다.");
        requestData.put("category", "일반");

        Faq expectedSavedFaq = createTestFaq(3L, "새로운 FAQ", "새로운 FAQ 내용입니다.", "일반");

        given(faqService.save(any(Faq.class))).willReturn(expectedSavedFaq);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/faq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.title").value("새로운 FAQ"))
                .andExpect(jsonPath("$.content").value("새로운 FAQ 내용입니다."))
                .andExpect(jsonPath("$.category").value("일반"));

        verify(faqService, times(1)).save(any(Faq.class));
    }

    @Test
    @DisplayName("FAQ 생성 - 필수 필드 누락")
    void createFaq_MissingRequiredFields() throws Exception {
        // Given
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "제목만 있는 FAQ");
        // content와 category는 누락

        Faq expectedSavedFaq = new Faq();
        expectedSavedFaq.setId(4L);
        expectedSavedFaq.setTitle("제목만 있는 FAQ");
        expectedSavedFaq.setContent(null);
        expectedSavedFaq.setCategory(null);

        given(faqService.save(any(Faq.class))).willReturn(expectedSavedFaq);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/faq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4L))
                .andExpect(jsonPath("$.title").value("제목만 있는 FAQ"));
    }

    @Test
    @DisplayName("FAQ 생성 - 서비스 예외 발생")
    void createFaq_ServiceException() throws Exception {
        // Given
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "에러 FAQ");
        requestData.put("content", "에러 내용");
        requestData.put("category", "에러");

        given(faqService.save(any(Faq.class)))
                .willThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/v1/admin/faq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error: Database connection failed")));

        verify(faqService, times(1)).save(any(Faq.class));
    }

    @Test
    @DisplayName("FAQ 생성 - 잘못된 JSON 형식")
    void createFaq_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/admin/faq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(faqService, never()).save(any(Faq.class));
    }

    @Test
    @DisplayName("FAQ 수정 - 성공")
    void updateFaq_Success() throws Exception {
        // Given
        Faq updateRequest = new Faq();
        updateRequest.setTitle("수정된 FAQ 제목");
        updateRequest.setContent("수정된 FAQ 내용");
        updateRequest.setCategory("수정된 카테고리");

        Faq updatedFaq = createTestFaq(1L, "수정된 FAQ 제목", "수정된 FAQ 내용", "수정된 카테고리");

        given(faqService.save(any(Faq.class))).willReturn(updatedFaq);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/faq/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("수정된 FAQ 제목"))
                .andExpect(jsonPath("$.content").value("수정된 FAQ 내용"))
                .andExpect(jsonPath("$.category").value("수정된 카테고리"));

        verify(faqService, times(1)).save(argThat(faq ->
                faq.getId().equals(1L) &&
                        faq.getTitle().equals("수정된 FAQ 제목")));
    }

    @Test
    @DisplayName("FAQ 수정 - 존재하지 않는 ID")
    void updateFaq_NotFound() throws Exception {
        // Given
        Faq updateRequest = new Faq();
        updateRequest.setTitle("수정된 FAQ 제목");
        updateRequest.setContent("수정된 FAQ 내용");
        updateRequest.setCategory("수정된 카테고리");

        given(faqService.save(any(Faq.class)))
                .willThrow(new RuntimeException("FAQ not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/api/v1/admin/faq/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("FAQ 삭제 - 성공")
    void deleteFaq_Success() throws Exception {
        // Given
        doNothing().when(faqService).delete(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/faq/1"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(faqService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("FAQ 삭제 - 존재하지 않는 ID")
    void deleteFaq_NotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("FAQ not found with id: 999"))
                .when(faqService).delete(999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/faq/999"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(faqService, times(1)).delete(999L);
    }
}