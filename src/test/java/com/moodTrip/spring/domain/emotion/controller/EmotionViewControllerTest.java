package com.moodTrip.spring.domain.emotion.controller;

import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.security.test.context.support.WithMockUser; // ✅ @WithMockUser 임포트
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmotionViewController.class)
class EmotionViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmotionService emotionService;

    @MockitoBean
    private AttractionService attractionService;

    @MockitoBean
    private SecurityUtil securityUtil;

    @BeforeEach
    void setUp() {
        Member testMember = Member.builder()
                .memberPk(1L)
                .memberId("testUser")
                .nickname("테스트유저")
                .isWithdraw(false)
                .build();

        when(securityUtil.getCurrentMember()).thenReturn(testMember);
        when(emotionService.getEmotionCategories()).thenReturn(Collections.emptyList());
    }

    @Nested
    @DisplayName("정상 조회 테스트")
    class SuccessCases {

        @Test
        @WithMockUser // ✅ 이 어노테이션으로 인증된 사용자를 시뮬레이션합니다.
        @DisplayName("초기 로딩 시 추천 여행지를 반환한다")
        void initialLoad_shouldReturnRecommendedAttractions() throws Exception {
            List<AttractionCardDTO> recommendedAttractions = List.of(new AttractionCardDTO());
            when(attractionService.findInitialAttractions(12)).thenReturn(recommendedAttractions);

            mockMvc.perform(get("/emotion-search"))
                    .andExpect(status().isOk()) // 이제 302가 아닌 200을 기대할 수 있습니다.
                    .andExpect(view().name("emotion-search/emotion-search"))
                    .andExpect(model().attribute("resultsTitle", "추천 여행지"))
                    .andExpect(model().attribute("attractions", recommendedAttractions));
        }

        @Test
        @WithMockUser // ✅ 모든 테스트에 인증을 추가합니다.
        @DisplayName("sort=popular 파라미터로 인기 여행지를 반환한다")
        void searchWithPopularSort_shouldReturnPopularAttractions() throws Exception {
            List<AttractionCardDTO> popularAttractions = List.of(new AttractionCardDTO(), new AttractionCardDTO());
            when(attractionService.findPopularAttractions(12)).thenReturn(popularAttractions);

            mockMvc.perform(get("/emotion-search").param("sort", "popular"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("emotion-search/emotion-search"))
                    .andExpect(model().attribute("resultsTitle", "인기 여행지"))
                    .andExpect(model().attribute("attractions", popularAttractions));
        }

        @Test
        @WithMockUser // ✅ 모든 테스트에 인증을 추가합니다.
        @DisplayName("tagId 파라미터로 감정 태그 기반 여행지를 반환한다")
        void searchWithTagId_shouldReturnAttractionsByTag() throws Exception {
            int tagId = 5;
            List<AttractionCardDTO> taggedAttractions = List.of(new AttractionCardDTO());
            when(attractionService.findAttractionsByEmotionTag(tagId, 12)).thenReturn(taggedAttractions);

            mockMvc.perform(get("/emotion-search").param("tagId", String.valueOf(tagId)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("emotion-search/emotion-search"))
                    .andExpect(model().attribute("resultsTitle", "여행지 검색 결과"))
                    .andExpect(model().attribute("attractions", taggedAttractions));
        }
    }

    @Nested
    @DisplayName("예외 처리 테스트")
    class ExceptionCases {

        @Test
        @WithMockUser // ✅ 예외 상황 테스트에도 인증이 필요합니다.
        @DisplayName("서비스에서 예외 발생 시 에러 메시지와 빈 리스트를 반환한다")
        void serviceThrowsException_shouldReturnEmptyListAndErrorMessage() throws Exception {
            when(attractionService.findInitialAttractions(anyInt())).thenThrow(new RuntimeException("DB connection failed"));

            mockMvc.perform(get("/emotion-search"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("emotion-search/emotion-search"))
                    .andExpect(model().attribute("resultsTitle", "검색 중 오류가 발생했습니다"))
                    .andExpect(model().attribute("attractions", Collections.emptyList()));
        }
    }
}
