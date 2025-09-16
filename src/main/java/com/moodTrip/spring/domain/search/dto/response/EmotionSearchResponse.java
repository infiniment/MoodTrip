/** package com.moodTrip.spring.domain.search.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionSearchResponse {

    // 태그 ID (M_emotion.id)
    private Long emotionId;

    // 감정 태그명 (M_emotion.tag_name)
    private String tagName;

    // 이 감정이 속한 카테고리 ID (M_emotion.emotion_category_id)
    private Long categoryId;

    // 카테고리 이름 (M_emotion_category.emotion_category_name)
    private String categoryName;
} */