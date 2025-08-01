/**
package com.moodTrip.spring.domain.search.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionSearchRequest {

    // 사용자가 선택한 감정 ID 목록 (예: [1, 3, 8])
    // URL: /api/v1/search/selected-emotions?emotion_ids=1,3,8 형태로 받을 예정
    private List<Long> emotionIds;
}
*/