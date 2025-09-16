package com.moodTrip.spring.domain.emotion.dto.response;

import com.moodTrip.spring.domain.emotion.dto.request.EmotionDto;
import com.moodTrip.spring.domain.emotion.entity.EmotionCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class EmotionCategoryDto {

    // DTO 객체
    private Long emotionCategoryId;
    // 대분류 카테고리 이름
    private String emotionCategoryName;
    //대분류 아이콘
    private String emotionCategoryIcon;

    //여러 소분류 감정들 
    private List<EmotionDto> emotions; // Thymeleaf의 ${category.emotions}와 매칭

    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static EmotionCategoryDto from(EmotionCategory category) {

        // Emotion 리스트를 EmotionDto 리스트로 변환
        List<EmotionDto> emotionDtos = category.getEmotions().stream()
                .map(EmotionDto::from)
                .collect(Collectors.toList());

        return EmotionCategoryDto.builder()
                .emotionCategoryId(category.getEmotionCategoryId())
                .emotionCategoryName(category.getEmotionCategoryName())
                .emotionCategoryIcon(category.getEmotionCategoryIcon())
                .emotions(emotionDtos) // 변환된 DTO 리스트를 설정
                .build();
    }
}