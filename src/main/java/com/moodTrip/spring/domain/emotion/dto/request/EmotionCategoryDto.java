package com.moodTrip.spring.domain.emotion.dto.request;

import com.moodTrip.spring.domain.emotion.entity.EmotionCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class EmotionCategoryDto {
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private List<EmotionDto> emotions; // 소분류 태그 목록

    public static EmotionCategoryDto from(EmotionCategory entity) {
        return EmotionCategoryDto.builder()
                .categoryId(entity.getEmotionCategoryId())
                .categoryName(entity.getEmotionCategoryName())
                .categoryIcon(entity.getEmotionCategoryIcon())
                .emotions(entity.getEmotions().stream() // 연관된 Emotion 리스트를 DTO로 변환
                        .map(EmotionDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}