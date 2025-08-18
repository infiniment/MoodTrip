package com.moodTrip.spring.domain.emotion.repository;

import com.moodTrip.spring.domain.emotion.entity.AttractionEmotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttractionEmotionRepository extends JpaRepository<AttractionEmotion, Long> {

    // isActive 필드가 true인 모든 AttractionEmotion 엔티티를 조회합니다.
    List<AttractionEmotion> findByIsActiveTrue(); // 이 메서드 선언을 추가합니다.

//    // 기존에 사용되던 다른 메서드들도 여기에 포함되어야 합니다.
//    Optional<AttractionEmotion> findByAttractionIdAndEmotion_TagId(Long attractionId, Long tagId);
//
//    // 특정 관광지의 활성화된 매핑 조회 (AttractionEmotionService에서 사용 중)
//    List<AttractionEmotion> findByAttractionIdAndIsActiveTrue(Long attractionId);

    Optional<AttractionEmotion> findByAttraction_AttractionIdAndEmotion_TagId(Long attractionId, Long tagId);
    List<AttractionEmotion> findByAttraction_AttractionIdAndIsActiveTrue(Long attractionId);



}