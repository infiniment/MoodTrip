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
    private final AttractionServiceImpl attractionService;

    @GetMapping("/emotion-search")
    public String emotionSearchPage(Model model) {

        // 1. 서비스에서 감정 카테고리 데이터(DTO 리스트)를 조회합니다.
        List<EmotionCategoryDto> emotionCategories = emotionService.getEmotionCategories();
        // 2. [추가] 초기 추천 여행지 6개를 조회
        List<AttractionCardDTO> initialAttractions = attractionService.findInitialAttractions(6);

        //  모델에 "emotionCategories"라는 이름으로 데이터를 추가합니다.
        //    (이 이름은 Thymeleaf의 th:each="category : ${emotionCategories}"와 일치해야 합니다.)
        model.addAttribute("emotionCategories", emotionCategories);
        model.addAttribute("initialAttractions", initialAttractions);
        // 3. 렌더링할 Thymeleaf 템플릿 파일의 경로를 반환합니다.
        //    (resources/templates/emotion-search/emotion-search.html)
        return "emotion-search/emotion-search";
    }

    @GetMapping("/emotions")
    public String searchAttractionsByEmotion(
            @RequestParam(required = false) Integer tagId, Model model) {

        // emotion-search 페이지 자체의 카테고리 목록 (결과 페이지에도 필요)
        model.addAttribute("emotionCategories", emotionService.getEmotionCategories());

        List<AttractionCardDTO> searchedAttractions;
        // tagId가 있으면 해당 감정으로 검색
        if (tagId != null) {
            searchedAttractions = attractionService.findAttractionsByEmotionTag(tagId, 6);
            model.addAttribute("resultsTitle", "여행지 검색 결과");
        } else {
            // tagId 없이 요청된 경우(예: 대분류만 선택), 초기 추천 목록을 보여줌
            searchedAttractions = attractionService.findInitialAttractions(6);
            model.addAttribute("resultsTitle", "추천 여행지");
        }

        model.addAttribute("attractions", searchedAttractions);
        model.addAttribute("resultsCount", searchedAttractions.size());

        // 검색 결과를 보여줄 템플릿으로 이동
        return "emotion-search/emotion-search";
    }




}
