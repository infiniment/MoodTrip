package com.moodTrip.spring.domain.emotion.repository;

import com.moodTrip.spring.domain.emotion.entity.AttractionEmotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttractionEmotionRepository extends JpaRepository<AttractionEmotion, Long> {
    // 주어진 관광지 ID에 대해 활성화된(사용 중인) 감정 매핑 리스트 조회
    List<AttractionEmotion> findByAttractionIdAndIsActiveTrue(Long attractionId);

    // 특정 관광지와 감정 조합으로 매핑 정보 조회 (중복등록 방지 및 수정용)
    Optional<AttractionEmotion> findByAttractionIdAndEmotion_TagId(Long attractionId, Long tagId);
}