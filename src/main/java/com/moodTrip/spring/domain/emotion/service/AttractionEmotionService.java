package com.moodTrip.spring.domain.emotion.service;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.emotion.dto.request.EmotionWeightDto;
import com.moodTrip.spring.domain.emotion.entity.AttractionEmotion;
import com.moodTrip.spring.domain.emotion.entity.Emotion; // Emotion 엔티티 import
import com.moodTrip.spring.domain.emotion.repository.AttractionEmotionRepository;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository; // EmotionRepository import

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AttractionEmotionService {

    @Autowired
    private AttractionEmotionRepository attractionEmotionRepository;

    @Autowired
    private EmotionRepository emotionRepository; // EmotionRepository 주입

    @Autowired
    private AttractionRepository attractionRepository;

    // 기존의 updateAttractionEmotionWeight 메서드는 삭제하지 않고 그대로 유지합니다.
    @Transactional
    public void updateAttractionEmotionWeight(Long attractionId, Long emotionId, BigDecimal weight) {
        // 기존 로직 유지
        Optional<AttractionEmotion> existingMapping = attractionEmotionRepository.findByAttraction_AttractionIdAndEmotion_TagId(attractionId, emotionId);
        if (existingMapping.isPresent()) {
            AttractionEmotion mapping = existingMapping.get();
            mapping.setWeight(weight);
            attractionEmotionRepository.save(mapping);
        } else {
            throw new IllegalArgumentException("존재하지 않는 매핑입니다.");
        }
    }

    @Transactional
    public void updateAttractionEmotions(Long attractionId, List<EmotionWeightDto> emotionWeights) {
        // 1) 기존 매핑을 모두 비활성화합니다.
        List<AttractionEmotion> existingActiveMappings = attractionEmotionRepository.findByAttraction_AttractionIdAndIsActiveTrue(attractionId);
        for (AttractionEmotion mapping : existingActiveMappings) {
            mapping.setActive(false);
        }
        attractionEmotionRepository.saveAll(existingActiveMappings);

        // 2) 클라이언트에서 전달된 emotionWeights 기반으로 매핑을 갱신하거나 새로 생성합니다.
        for (EmotionWeightDto ew : emotionWeights) {
            Optional<AttractionEmotion> existing = attractionEmotionRepository.findByAttraction_AttractionIdAndEmotion_TagId(attractionId, ew.getEmotionId());
            AttractionEmotion mapping;

            if (existing.isPresent()) {
                mapping = existing.get();
                mapping.setWeight(ew.getWeight());
                mapping.setActive(true);
            } else {
                Emotion emotion = emotionRepository.findById(ew.getEmotionId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 감정입니다: " + ew.getEmotionId()));

                Attraction attraction = attractionRepository.findById(attractionId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관광지입니다: " + attractionId));

                mapping = AttractionEmotion.builder()
                        .attraction(attraction)
                        .emotion(emotion)
                        .weight(ew.getWeight())
                        .isActive(true)
                        .build();
            }
            attractionEmotionRepository.save(mapping);
        }
    }

    public Map<Long, List<Long>> getAttractionToEmotionIdsMap() {
        Map<Long, List<Long>> map = new HashMap<>();
        List<AttractionEmotion> allActiveMappings = attractionEmotionRepository.findByIsActiveTrue();

        for (AttractionEmotion mapping : allActiveMappings) {
            Long attractionId = mapping.getAttraction().getAttractionId();
            // Integer 타입인 tagId를 Long으로 변환
            Long emotionId = mapping.getEmotion().getTagId().longValue(); // <--- 수정된 부분

            map.computeIfAbsent(attractionId, k -> new ArrayList<>()).add(emotionId);
        }
        return map;
    }

    public Map<Long, Map<Long, BigDecimal>> getAttractionToEmotionWeightsMap() {
        Map<Long, Map<Long, BigDecimal>> map = new HashMap<>();
        List<AttractionEmotion> allActiveMappings = attractionEmotionRepository.findByIsActiveTrue();

        for (AttractionEmotion mapping : allActiveMappings) {
            Long attractionId = mapping.getAttraction().getAttractionId();
            // Integer 타입인 tagId를 Long으로 변환
            Long emotionId = mapping.getEmotion().getTagId().longValue(); // <--- 수정된 부분
            BigDecimal weight = mapping.getWeight();

            map.computeIfAbsent(attractionId, k -> new HashMap<>()).put(emotionId, weight);
        }
        return map;
    }

    @Transactional(readOnly = true)
    public List<String> findTagNamesByContentId(long contentId) {
        List<String> names = attractionEmotionRepository.findTagNamesByContentId(contentId);
        return names != null ? names : Collections.emptyList();
    }

}
