package com.moodTrip.spring.domain.emotion.controller;

import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.attraction.service.AttractionServiceImpl;
import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import com.moodTrip.spring.domain.emotion.dto.response.EmotionCategoryDto;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EmotionViewController {

    private final EmotionService emotionService;
    private final AttractionService attractionService; // 인터페이스 타입으로 변경하는 것을 권장합니다.

    @GetMapping("/emotion-search") // URL을 '/emotion-search'로 통합
    public String emotionSearch(
            @RequestParam(required = false) Integer tagId,
            @RequestParam(required = false, defaultValue = "recommended") String sort, // 정렬 파라미터 추가
            Model model) {

        // 1. 모든 경우에 필요한 감정 카테고리 목록을 모델에 추가
        model.addAttribute("emotionCategories", emotionService.getEmotionCategories());

        List<AttractionCardDTO> attractions;
        String title;

        // 2. 'sort' 파라미터 값에 따라 분기 처리
        if ("popular".equals(sort)) {
            // "인기순" 정렬 요청이 오면
            attractions = attractionService.findPopularAttractions(12); // 인기 여행지 12개 조회
            title = "인기 여행지";
        } else {
            // 그 외의 경우 (기본값: "recommended")
            if (tagId != null) {
                // 감정 태그가 선택되었으면 태그 기반으로 검색
                attractions = attractionService.findAttractionsByEmotionTag(tagId, 12);
                title = "여행지 검색 결과";
            } else {
                // 아무 조건도 없으면 초기 추천 여행지 표시
                attractions = attractionService.findInitialAttractions(12);
                title = "추천 여행지";
            }
        }

        // 3. 최종 결과를 모델에 담기
        model.addAttribute("attractions", attractions);
        model.addAttribute("resultsCount", attractions.size());
        model.addAttribute("resultsTitle", title);
        model.addAttribute("currentSort", sort); // [추가] 현재 정렬 상태를 뷰에 전달

        // 4. 템플릿 렌더링
        return "emotion-search/emotion-search";
    }
}
