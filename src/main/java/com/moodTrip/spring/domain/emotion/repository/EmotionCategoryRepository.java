package com.moodTrip.spring.domain.emotion.repository;

import com.moodTrip.spring.domain.emotion.entity.EmotionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmotionCategoryRepository extends JpaRepository<EmotionCategory, Long> {

    //감정 카테고리(EmotionCategory)와 그에 속한 감정 태그(Emotion) 데이터를 한 번의 DB 조회
    @Query("SELECT DISTINCT ec FROM EmotionCategory ec " +
            "LEFT JOIN FETCH ec.emotions e " +
            "ORDER BY ec.displayOrder, e.displayOrder")
    List<EmotionCategory> findAllWithEmotions();

}