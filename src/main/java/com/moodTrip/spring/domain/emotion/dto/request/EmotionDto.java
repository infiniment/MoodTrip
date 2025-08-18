package com.moodTrip.spring.domain.emotion.dto.request;

import com.moodTrip.spring.domain.emotion.entity.Emotion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionDto {
    private Integer tagId;
    private String tagName;

    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static EmotionDto from(Emotion emotion) {
        return EmotionDto.builder()
                .tagId(emotion.getTagId())
                .tagName(emotion.getTagName())
                .build();
    }
}