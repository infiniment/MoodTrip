/** package com.moodTrip.spring.domain.search.service;

import com.moodTrip.spring.domain.search.dto.request.EmotionSearchRequest;
import com.moodTrip.spring.domain.search.dto.response.EmotionSearchResponse;
import com.moodTrip.spring.domain.search.repository.EmotionSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmotionSearchService {

    private final EmotionSearchRepository emotionSearchRepository;

     * 1. 사용자가 선택한 감정들의 상세 정보 조회
     * 2. "선택된 감정: 행복, 슬픔, 배고픔" 화면 표시용
     * 3. 잘못된 감정 ID는 자동으로 제외됨 (Repository에서 처리)

    public List<EmotionSearchResponse> getSelectedEmotions(EmotionSearchRequest request) {
        log.info("선택된 감정 조회 시작 - emotionIds: {}", request.getEmotionIds());

        // 1. 요청 검증
        if (request.getEmotionIds() == null || request.getEmotionIds().isEmpty()) {
            log.warn("감정 ID 목록이 비어있습니다.");
            return List.of();
        }

        // 2. DB에서 선택된 감정들의 상세 정보 조회
        // 존재하지 않는 ID는 자동으로 제외됨!
        List<Emotion> emotions = emotionSearchRepository.findByIdInOrderByDisplayOrderAsc(request.getEmotionIds());

        // 3. Entity를 Response DTO로 변환
        List<EmotionSearchResponse> responses = emotions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        log.info("선택된 감정 조회 완료 - 조회된 감정: {}",
                responses.stream().map(EmotionSearchResponse::getTagName).collect(Collectors.toList()));

        return responses;
    }

    private EmotionSearchResponse convertToResponse(Emotion emotion) {
        // TODO: 나중에 EmotionCategory 조회해서 실제 카테고리명 설정
        String categoryName = "카테고리명"; // 임시값

        return EmotionSearchResponse.builder()
                .emotionId(emotion.getId())
                .tagName(emotion.getTagName())                 // "행복", "슬픔", "배고픔"
                .categoryId(emotion.getEmotionCategoryId())
                .categoryName(categoryName)
                .build();
    }
} */