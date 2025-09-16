/** package com.moodTrip.spring.domain.search.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// 헤더에서 특정 카테고리의 감정들을 조회하는 Repository
@Repository
public interface EmotionSearchRepository extends JpaRepository<Emotion, Long> {
    // 사용자가 선택한 감정들의 정보 조회하기.
    List<Emotion> findByIdInOrderByDisplayOrderAsc(List<Long> emotionIds);
}*/