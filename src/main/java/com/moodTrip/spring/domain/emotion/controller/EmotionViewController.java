package com.moodTrip.spring.domain.emotion.controller;

import com.moodTrip.spring.domain.emotion.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class EmotionViewController {

    private final EmotionService emotionService;

    @GetMapping("/emotion-search") // 예시 URL
    public String emotionSearchPage(Model model) {
        // Service를 통해 모든 감정 카테고리와 태그 데이터를 조회
        model.addAttribute("emotionCategories", emotionService.findAllEmotionData());
        return "emotion-search/emotion-search"; // resources/templates/emotion-search/emotion-search.html
    }
}
