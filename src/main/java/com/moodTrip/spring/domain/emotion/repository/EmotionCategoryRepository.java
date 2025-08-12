package com.moodTrip.spring.domain.emotion.repository;

import com.moodTrip.spring.domain.emotion.entity.EmotionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmotionCategoryRepository extends JpaRepository<EmotionCategory, Long> {

    // ==============================================================
    // 이 메서드를 추가해주세요.
    // fetch join을 사용하여 EmotionCategory를 조회할 때
    // 연관된 emotions 리스트를 함께 가져와서 N+1 문제를 방지합니다.
    // ==============================================================
    @Query("SELECT DISTINCT ec FROM EmotionCategory ec " +
            "LEFT JOIN FETCH ec.emotions e " +
            "ORDER BY ec.displayOrder, e.displayOrder")
    List<EmotionCategory> findAllWithEmotions();

}