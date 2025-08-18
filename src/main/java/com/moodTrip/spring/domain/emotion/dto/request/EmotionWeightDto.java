package com.moodTrip.spring.domain.emotion.dto.request;

import java.math.BigDecimal;

public class EmotionWeightDto {
    private Long emotionId;      // 감정 태그 ID
        private BigDecimal weight;   // 가중치 값

        public EmotionWeightDto() {}

        public EmotionWeightDto(Long emotionId, BigDecimal weight) {
            this.emotionId = emotionId;
            this.weight = weight;
        }

        public Long getEmotionId() {
            return emotionId;
        }

        public void setEmotionId(Long emotionId) {
            this.emotionId = emotionId;
        }

        public BigDecimal getWeight() {
            return weight;
        }

        public void setWeight(BigDecimal weight) {
            this.weight = weight;
        }
}
