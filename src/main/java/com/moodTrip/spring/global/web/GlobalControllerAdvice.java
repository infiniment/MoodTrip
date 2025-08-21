package com.moodTrip.spring.global.web;


import com.moodTrip.spring.domain.emotion.dto.response.EmotionCategoryDto;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final EmotionService emotionService;

    // "headerEmotionCategories"라는 이름으로 모델에 데이터를 추가하는 메서드
    // 모든 컨트롤러가 실행되기 전에 호출되어, 어느 페이지에서든 해당 데이터를 사용 가능
    @ModelAttribute("headerEmotionCategories")
    public List<EmotionCategoryDto> addHeaderEmotionCategoriesToModel() {
        return emotionService.getEmotionCategories();
    }
}