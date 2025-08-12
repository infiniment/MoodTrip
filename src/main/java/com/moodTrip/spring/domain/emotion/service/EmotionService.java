package com.moodTrip.spring.domain.emotion.service;

import com.moodTrip.spring.domain.emotion.dto.request.EmotionCategoryDto;
import com.moodTrip.spring.domain.emotion.repository.EmotionCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmotionService {

    private final EmotionCategoryRepository emotionCategoryRepository;

    public List<EmotionCategoryDto> findAllEmotionData() {
        // N+1 문제 해결을 위해 fetch join 사용을 권장합니다.
        return emotionCategoryRepository.findAllWithEmotions().stream()
                .map(EmotionCategoryDto::from)
                .collect(Collectors.toList());
    }
}