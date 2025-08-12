package com.moodTrip.spring.domain.emotion.controller;

import com.moodTrip.spring.domain.emotion.dto.response.EmotionCategoryDto;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EmotionViewController {

    private final EmotionService emotionService;

    @GetMapping("/emotion-search")
    public String emotionSearchPage(Model model) {

        // 1. 서비스에서 감정 카테고리 데이터(DTO 리스트)를 조회합니다.
        List<EmotionCategoryDto> emotionCategories = emotionService.getEmotionCategories();

        // 2. 모델에 "emotionCategories"라는 이름으로 데이터를 추가합니다.
        //    (이 이름은 Thymeleaf의 th:each="category : ${emotionCategories}"와 일치해야 합니다.)
        model.addAttribute("emotionCategories", emotionCategories);

        // 3. 렌더링할 Thymeleaf 템플릿 파일의 경로를 반환합니다.
        //    (resources/templates/emotion-search/emotion-search.html)
        return "emotion-search/emotion-search";
    }
}
