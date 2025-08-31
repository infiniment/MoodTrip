package com.moodTrip.spring.domain.weather.service;

import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 날씨(main/description) → 감정(Emotion) 매퍼
 * - 카테고리 ID / 이름 매핑 (대분류 추천용)
 * - 태그명 매핑 (Emotion 엔티티 직접 매핑용)
 */
@Component
@RequiredArgsConstructor
public class WeatherEmotionMapper {

    private final EmotionRepository emotionRepository;

    /** 날씨(main) → 대분류 카테고리 ID 리스트 */
    private static final Map<String, List<Long>> CAT_IDS = Map.of(
            "Clear",        List.of(5L, 4L, 10L),   // 기쁨 & 즐거움, 자유 & 해방, 희망 & 긍정
            "Clouds",       List.of(1L, 8L, 6L),    // 평온 & 힐링, 성찰 & 사색, 감성 & 예술
            "Rain",         List.of(9L, 11L, 6L),   // 위로 & 공감, 우울 & 슬픔, 감성 & 예술
            "Drizzle",      List.of(9L, 11L, 1L),   // 위로 & 공감, 우울 & 슬픔, 평온 & 힐링
            "Thunderstorm", List.of(3L, 7L, 15L),   // 모험 & 스릴, 열정 & 에너지, 놀라움 & 신기함
            "Snow",         List.of(1L, 5L, 6L),    // 평온 & 힐링, 기쁨 & 즐거움, 감성 & 예술
            "Mist",         List.of(8L, 6L, 1L),    // 성찰 & 사색, 감성 & 예술, 평온 & 힐링
            "Fog",          List.of(8L, 6L, 1L)     // 성찰 & 사색, 감성 & 예술, 평온 & 힐링
    );

    /** 날씨(main) → 대분류 카테고리 이름 리스트 */
    private static final Map<String, List<String>> CAT_NAMES = Map.of(
            "Clear",        List.of("기쁨 & 즐거움", "자유 & 해방", "희망 & 긍정"),
            "Clouds",       List.of("평온 & 힐링", "성찰 & 사색", "감성 & 예술"),
            "Rain",         List.of("위로 & 공감", "우울 & 슬픔", "감성 & 예술"),
            "Drizzle",      List.of("위로 & 공감", "우울 & 슬픔", "평온 & 힐링"),
            "Thunderstorm", List.of("모험 & 스릴", "열정 & 에너지", "놀라움 & 신기함"),
            "Snow",         List.of("평온 & 힐링", "기쁨 & 즐거움", "감성 & 예술"),
            "Mist",         List.of("성찰 & 사색", "감성 & 예술", "평온 & 힐링"),
            "Fog",          List.of("성찰 & 사색", "감성 & 예술", "평온 & 힐링")
    );

    /** 날씨(main/description) → Emotion 태그 매핑 */
    private static final Map<String, String> WEATHER_TO_TAG = Map.ofEntries(
            Map.entry("clear", "Sunny"),
            Map.entry("clouds", "Cloudy"),
            Map.entry("rain", "Rainy"),
            Map.entry("drizzle", "Rainy"),
            Map.entry("thunderstorm", "Stormy"),
            Map.entry("snow", "Snowy"),
            Map.entry("mist", "Calm"),
            Map.entry("fog", "Calm"),
            Map.entry("haze", "Calm")
    );

    /** 대분류 카테고리 ID 조회 */
    public List<Long> categoryIdsFor(String weatherMain) {
        return CAT_IDS.getOrDefault(weatherMain, List.of(5L)); // 기본: 기쁨 & 즐거움
    }

    /** 대분류 카테고리 이름 조회 */
    public List<String> categoriesFor(String weatherMain) {
        return CAT_NAMES.getOrDefault(weatherMain, List.of("기쁨 & 즐거움"));
    }

    /** 날씨 main/desc → Emotion 엔티티 매핑 */
    public Emotion mapToEmotion(String mainOrDesc) {
        if (mainOrDesc == null) {
            return getDefaultEmotion();
        }

        String key = mainOrDesc.trim().toLowerCase();
        String tagName = WEATHER_TO_TAG.getOrDefault(key, "Sunny");

        return emotionRepository.findByTagName(tagName)
                .orElseGet(this::getDefaultEmotion);
    }

    /** 기본 Emotion 반환 (Sunny 없으면 첫 번째라도 반환) */
    private Emotion getDefaultEmotion() {
        return emotionRepository.findByTagName("Sunny")
                .orElseGet(() -> emotionRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No Emotion configured at all")));
    }
}
