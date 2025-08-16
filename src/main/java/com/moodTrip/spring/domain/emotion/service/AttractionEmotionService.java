package com.moodTrip.spring.domain.emotion.service;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.emotion.dto.request.EmotionWeightDto;
import com.moodTrip.spring.domain.emotion.entity.AttractionEmotion;
import com.moodTrip.spring.domain.emotion.repository.AttractionEmotionRepository;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@AllArgsConstructor
public class AttractionEmotionService {

    private final AttractionRepository attractionRepository;
    private final EmotionRepository emotionRepository;
    private final AttractionEmotionRepository attractionEmotionRepository;



    // 모든 관광지 리스트 조회
    public List<Attraction> getAllAttractions() {
        return attractionRepository.findAll();
    }

    // 모든 소분류 감정 리스트 조회
    public List<Emotion> getAllEmotions() {
        return emotionRepository.findAll();
    }

    // 특정 관광지에 매핑된 활성화된 감정 매핑 정보 조회
    public List<AttractionEmotion> getActiveEmotionsByAttraction(Long attractionId) {
        return attractionEmotionRepository.findByAttractionIdAndIsActiveTrue(attractionId);
    }

    // 관광지의 감정 매핑 업데이트 처리 메서드
    @Transactional
    public void updateAttractionEmotions(Long attractionId, List<EmotionWeightDto> emotionWeights) {
        log.info("POST updateAttractionEmotion called - attractionId: {}", attractionId);

        // 1) 기존 활성화된 매핑 모두 비활성화 처리하여 초기화
        List<AttractionEmotion> existingMappings = attractionEmotionRepository.findByAttractionIdAndIsActiveTrue(attractionId);
        log.info("Found {} existing active mappings", existingMappings.size());
        for (AttractionEmotion mapping : existingMappings) {
            log.debug("Deactivating mapping id: {}, tagId: {}", mapping.getId(), mapping.getEmotion().getTagId());
            mapping.setActive(false);
        }
        attractionEmotionRepository.saveAll(existingMappings);
        log.info("Existing mappings deactivated.");

        // 2) 입력된 감정 태그와 weight 정보를 기반으로 신규 매핑 생성 또는 기존 매핑 재활성화 및 수정
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> {
                    log.error("Invalid attractionId: {}", attractionId);
                    return new IllegalArgumentException("Invalid attractionId");
                });

        for (EmotionWeightDto ew : emotionWeights) {
            Optional<AttractionEmotion> existing = attractionEmotionRepository.findByAttractionIdAndEmotion_TagId(attractionId, ew.getEmotionId());
            AttractionEmotion mapping;
            if (existing.isPresent()) {
                mapping = existing.get();
                log.debug("Updating existing mapping id: {}, weight: {}", mapping.getId(), ew.getWeight());
                mapping.setWeight(ew.getWeight());
                mapping.setActive(true);
            } else {
                Emotion emotion = emotionRepository.findById(ew.getEmotionId())
                        .orElseThrow(() -> {
                            log.error("Invalid emotionId: {}", ew.getEmotionId());
                            return new IllegalArgumentException("Invalid emotionId");
                        });
                mapping = AttractionEmotion.builder()
                        .attraction(attraction)
                        .emotion(emotion)
                        .weight(ew.getWeight())
                        .isActive(true)
                        .build();
                log.debug("Creating new mapping for attractionId: {}, emotionId: {}, weight: {}", attractionId, ew.getEmotionId(), ew.getWeight());
            }
            attractionEmotionRepository.save(mapping);
        }

        log.info("updateAttractionEmotions completed successfully for attractionId: {}", attractionId);
    }



    public List<Attraction> getTop10Attractions() {
        // 예: 생성일(createdTime) 기준으로 최근 10개 관광지 조회
        return attractionRepository.findTop10ByOrderByCreatedTimeDesc();
    }
}
