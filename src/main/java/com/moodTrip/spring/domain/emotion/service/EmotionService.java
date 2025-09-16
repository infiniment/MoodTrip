package com.moodTrip.spring.domain.emotion.service;

import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.emotion.dto.response.EmotionCategoryDto;
import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.emotion.entity.EmotionCategory;
import com.moodTrip.spring.domain.emotion.repository.EmotionCategoryRepository;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // ArrayList import
import java.util.LinkedHashSet; // LinkedHashSet import
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmotionService {

    private final EmotionCategoryRepository emotionCategoryRepository;
    private final EmotionRepository emotionRepository;

    public List<EmotionCategoryDto> getEmotionCategories() {

        log.info("🔍 === getEmotionCategories 디버깅 시작 ===");

        List<EmotionCategory> categoriesWithDuplicates = emotionCategoryRepository.findAllWithEmotions();

        log.info("📊 조회된 총 개수: {}", categoriesWithDuplicates.size());

        // 각 카테고리 상세 확인
        for (int i = 0; i < categoriesWithDuplicates.size(); i++) {
            EmotionCategory category = categoriesWithDuplicates.get(i);
            log.info("📋 [{}] 카테고리: {} (ID: {})",
                    i, category.getEmotionCategoryName(), category.getEmotionCategoryId());

            if (category.getEmotions() != null) {
                log.info("    └─ emotions 개수: {}", category.getEmotions().size());
                for (int j = 0; j < Math.min(3, category.getEmotions().size()); j++) {
                    log.info("       [{}] {}", j, category.getEmotions().get(j).getTagName());
                }
            } else {
                log.error("    └─ ❌ emotions가 null!");
            }
        }

        List<EmotionCategory> distinctCategories = new ArrayList<>(new LinkedHashSet<>(categoriesWithDuplicates));
        log.info("🔄 중복 제거 후 개수: {}", distinctCategories.size());

        List<EmotionCategoryDto> result = distinctCategories.stream()
                .map(EmotionCategoryDto::from)
                .collect(Collectors.toList());

        log.info("✅ DTO 변환 후 개수: {}", result.size());

        // 첫 번째 DTO 상세 확인
        if (!result.isEmpty()) {
            EmotionCategoryDto firstDto = result.get(0);
            log.info("🎯 첫 번째 DTO: {} (ID: {})",
                    firstDto.getEmotionCategoryName(),
                    firstDto.getEmotionCategoryId());
            if (firstDto.getEmotions() != null) {
                log.info("    └─ DTO의 emotions 개수: {}", firstDto.getEmotions().size());
            } else {
                log.error("    └─ ❌ DTO의 emotions가 null!");
            }
        }

        log.info("🔍 === getEmotionCategories 디버깅 종료 ===");

        return result;
    }


    public List<Emotion> getAllEmotions() {
        return emotionRepository.findAll();
    }

}
