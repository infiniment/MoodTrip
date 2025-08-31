package com.moodTrip.spring.domain.weather.service;

import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository;
import com.moodTrip.spring.domain.emotion.service.AttractionEmotionService;
import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeatherAttractionService {
    private final WeatherService weatherService;
    private final WeatherEmotionMapper mapper;
    private final EmotionRepository emotionRepository;
    private final AttractionService attractionService;
    private final AttractionEmotionService attractionEmotionService;

    public List<AttractionCardDTO> recommendByCoord(double lat, double lon) {
        var w = weatherService.getCurrentWeather(lat, lon);
        return recommendInternal(normalize(w.getWeather()));
    }

    public List<AttractionCardDTO> recommendByRoom(Long roomId) {
        var w = weatherService.getCurrentByRoom(roomId);
        return recommendInternal(normalize(w.getWeather()));
    }

    private List<AttractionCardDTO> recommendInternal(String weatherMain) {
        var cats = mapper.categoriesFor(weatherMain); // ["기쁨 & 즐거움", ...]
        var emotionIds = emotionRepository
                .findByEmotionCategory_EmotionCategoryNameIn(cats)
                .stream()
                .map(Emotion::getTagId)
                .toList();
        return emotionIds.isEmpty() ? List.of()
                : attractionService.findAttractionsByEmotionIds(emotionIds);
    }

    // ✅ 한글 → 영문 키 정규화
    private String normalize(String s) {
        if (s == null) return "Clear";
        return switch (s.trim()) {
            case "맑음" -> "Clear";
            case "흐림" -> "Clouds";
            case "비" -> "Rain";
            case "이슬비" -> "Drizzle";
            case "눈" -> "Snow";
            case "안개" -> "Mist";
            case "뇌우" -> "Thunderstorm";
            default -> s; // 이미 영문이면 그대로
        };
    }


    @Transactional(readOnly = true)
    public List<AttractionResponse> getSeoulAttractionsByWeather(Long contentId) {
        // 1) 현재 서울 날씨
        WeatherResponse weather = weatherService.getSeoulCurrentWeather(contentId);

        // 2) 날씨 메인/설명 → Emotion 매핑
        String base = weather.getWeather() != null ? weather.getWeather() : weather.getDescription();
        Emotion emotion = mapper.mapToEmotion(base);

        // 3) 감정 기반 관광지 추천
        return attractionEmotionService.findAttractionsByEmotion(emotion.getTagId().longValue());
    }

    /**
     * 특정 관광지(contentId)의 감정 태그 문자열 조회
     * → weather-detail-page.html 에서 tags 에 내려줌
     */
    @Transactional(readOnly = true)
    public List<String> getTagsByContentId(Long contentId) {
        return attractionEmotionService.findEmotionNamesByContentId(contentId);
    }

}
