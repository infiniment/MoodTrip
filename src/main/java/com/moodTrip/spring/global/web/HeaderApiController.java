package com.moodTrip.spring.global.web;

import com.moodTrip.spring.domain.emotion.dto.response.EmotionCategoryDto;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HeaderApiController {  // 클래스명 변경

    private final EmotionService emotionService;

    @GetMapping("/categories")
    public ResponseEntity<List<EmotionCategoryDto>> getEmotionCategories() {
        List<EmotionCategoryDto> categories = emotionService.getEmotionCategories();
        return ResponseEntity.ok(categories);
    }
}