package com.moodTrip.spring.domain.emotion.dto.request;

import com.moodTrip.spring.domain.emotion.entity.Emotion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionDto {
    private Integer tagId;
    private String tagName;
    private Integer displayOrder;

    public static EmotionDto from(Emotion entity) {
        return EmotionDto.builder()
                .tagId(entity.getTagId())
                .tagName(entity.getTagName())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }
}