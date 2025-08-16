package com.moodTrip.spring.domain.emotion.repository;

import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.rooms.entity.EmotionRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmotionRepository extends JpaRepository<Emotion, Long> {
    // 특정 감정 대분류에 속한 소분류 감정 리스트 조회 메서드 예시
    List<Emotion> findByEmotionCategory_EmotionCategoryId(Long emotionCategoryId);
}
