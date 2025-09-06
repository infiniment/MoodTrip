package com.moodTrip.spring.domain.emotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.attraction.service.AttractionServiceImpl;
import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import com.moodTrip.spring.global.config.SecurityConfig;
import com.moodTrip.spring.global.security.jwt.JwtUtil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService; // ✅ 마지막 의존성 클래스 임포트
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttractionSearchController.class)
@Import(SecurityConfig.class)
class AttractionSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- @WebMvcTest 환경에서 찾지 못하는 모든 의존성들을 MockBean으로 등록 ---

    // 1. 테스트 대상 컨트롤러가 직접 의존하는 서비스
    @MockitoBean
    private AttractionServiceImpl attractionSearchService;

    // 2. 각종 Advice 및 Config가 의존하는 서비스들
    @MockitoBean
    private SecurityUtil securityUtil;

    @MockitoBean
    private EmotionService emotionService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // ✅ 3. SecurityConfig의 filterChain이 필요로 하는 UserDetailsService 추가
    @MockitoBean
    private UserDetailsService userDetailsService;


    @Test
    @WithMockUser
    void searchAttractionsByEmotions_shouldReturnOkWithAttractions() throws Exception {
        // --- 준비 (Arrange) ---
        List<AttractionCardDTO> mockAttractions = List.of(
                AttractionCardDTO.builder()
                        .contentId(1L)
                        .attractionId(101L)
                        .title("행복한 놀이공원")
                        .addr1("서울")
                        .firstImage("http://example.com/image1.jpg")
                        .build()
        );
        when(attractionSearchService.findAttractionsByEmotionIds(anyList())).thenReturn(mockAttractions);

        // --- 실행 (Act) & 검증 (Assert) ---
        mockMvc.perform(get("/api/attractions/search")
                        .param("emotionIds", "1", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("행복한 놀이공원"));
    }

    @Test
    @WithMockUser
    void searchAttractionsByEmotions_shouldReturnEmptyListWhenNotFound() throws Exception {
        // --- 준비 (Arrange) ---
        when(attractionSearchService.findAttractionsByEmotionIds(anyList())).thenReturn(List.of());

        // --- 실행 (Act) & 검증 (Assert) ---
        mockMvc.perform(get("/api/attractions/search")
                        .param("emotionIds", "99"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }
}
