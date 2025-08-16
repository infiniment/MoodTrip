package com.moodTrip.spring.domain.emotion.service;

import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.emotion.dto.response.EmotionCategoryDto;
import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.emotion.entity.EmotionCategory;
import com.moodTrip.spring.domain.emotion.repository.EmotionCategoryRepository;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // ArrayList import
import java.util.LinkedHashSet; // LinkedHashSet import
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmotionService {

    private final EmotionCategoryRepository emotionCategoryRepository;
    private final EmotionRepository emotionRepository;

    public List<EmotionCategoryDto> getEmotionCategories() {

        // 1. 리포지토리에서 데이터를 가져오면 이 리스트에는 중복된 EmotionCategory가 포함될수도
        List<EmotionCategory> categoriesWithDuplicates = emotionCategoryRepository.findAllWithEmotions();

        // 2. LinkedHashSet을 사용하여 혹시모를 중복 제거
        List<EmotionCategory> distinctCategories = new ArrayList<>(new LinkedHashSet<>(categoriesWithDuplicates));

        // 3. 중복이 제거된 순수한 리스트를 DTO로 변환하여 반환 (Response 객체, 추후 쓸 일?)
        return distinctCategories.stream()
                .map(EmotionCategoryDto::from)
                .collect(Collectors.toList());
    }


    public List<Emotion> getAllEmotions() {
        return emotionRepository.findAll(); // EmotionRepository를 사용하여 모든 Emotion 엔티티 조회
    }

}
