package com.moodTrip.spring.domain.weather.service;

import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository;
import com.moodTrip.spring.domain.emotion.service.AttractionEmotionService;
import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import com.moodTrip.spring.domain.weather.dto.response.MainPageWeatherAttractionResponse;
import com.moodTrip.spring.domain.weather.entity.WeatherAttraction;
import com.moodTrip.spring.domain.weather.repository.WeatherAttractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeatherAttractionService {
    private final WeatherService weatherService;
    private final WeatherEmotionMapper mapper;
    private final EmotionRepository emotionRepository;
    private final AttractionService attractionService;
    private final AttractionEmotionService attractionEmotionService;
    private final AttractionRepository attractionRepository;
    private final WeatherAttractionRepository weatherAttractionRepository;

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

    // 한글 → 영문 키 정규화
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
     */
    @Transactional(readOnly = true)
    public List<String> getTagsByContentId(Long contentId) {
        return attractionEmotionService.findEmotionNamesByContentId(contentId);
    }

    /**
     * 메인페이지용 실제 현재 날씨 기반 추천
     * - 각 관광지의 실제 현재 날씨 사용
     * - 매번 다른 관광지 조합 (랜덤 셔플)
     * - 오늘 날씨만 사용
     */
    public List<MainPageWeatherAttractionResponse> getMainPageWeatherRecommendations() {
        try {
            log.info("=== 실제 현재 날씨 기반 추천 시작 ===");

            List<MainPageWeatherAttractionResponse> recommendations = new ArrayList<>();

            // 1단계: 관광지 목록 조회 및 랜덤 셔플
            List<Long> attractionIds = getAvailableAttractionIds();
            log.info("전체 관광지: {}개", attractionIds.size());

            if (attractionIds.isEmpty()) {
                log.warn("관광지 데이터 없음, 기본 추천 반환");
                return createDefaultRecommendations();
            }

            // 매번 다른 순서로 섞기 (핵심 기능)
            Collections.shuffle(attractionIds);
            log.info("관광지 순서 랜덤 셔플 완료");

            // 2단계: 셔플된 순서로 3개 관광지의 실제 현재 날씨 조회
            int successCount = 0;
            for (int i = 0; i < attractionIds.size() && successCount < 3; i++) {
                try {
                    Long attractionId = attractionIds.get(i);

                    // 관광지 정보 조회
                    Attraction attraction = attractionRepository.findById(attractionId).orElse(null);
                    if (attraction == null) {
                        log.warn("관광지 조회 실패 - ID: {}", attractionId);
                        continue;
                    }

                    log.info("{}번째 관광지 처리: {} (ID: {})", i + 1, attraction.getTitle(), attractionId);

                    // 해당 관광지의 실제 현재 날씨 조회
                    WeatherResponse realCurrentWeather = getCurrentWeatherForAttraction(attraction);

                    if (realCurrentWeather == null) {
                        log.warn("관광지 {}의 날씨 조회 실패, 서울 날씨로 대체", attraction.getTitle());
                        realCurrentWeather = getCurrentSeoulWeather();
                    }

                    if (realCurrentWeather == null) {
                        log.warn("서울 날씨도 조회 실패, 해당 관광지 건너뛰기");
                        continue;
                    }

                    log.info("실제 날씨 조회 성공: {} {}°C (습도: {}%)",
                            realCurrentWeather.getWeather(),
                            Math.round(realCurrentWeather.getTemperature()),
                            realCurrentWeather.getHumidity());

                    // 감정 태그 조회
                    List<String> tags = getAttractionTags(attractionId);

                    // 실제 날씨 데이터로 Response 생성
                    MainPageWeatherAttractionResponse response = createRealWeatherResponse(
                            attraction, realCurrentWeather, tags);

                    recommendations.add(response);
                    successCount++;

                    log.info("{}번째 추천 완성: {} - 실제 {}°C {}",
                            successCount, attraction.getTitle(),
                            Math.round(realCurrentWeather.getTemperature()),
                            realCurrentWeather.getWeather());

                } catch (Exception e) {
                    log.warn("{}번째 관광지 처리 중 오류: {}", i + 1, e.getMessage());
                }
            }

            // 3단계: 부족한 개수만큼 백업 추천 추가
            WeatherResponse seoulWeather = getCurrentSeoulWeather();
            while (recommendations.size() < 3) {
                String backupWeather = seoulWeather != null ? seoulWeather.getWeather() : "Clear";
                recommendations.add(createBackupRecommendation(recommendations.size(), backupWeather));
            }

            log.info("=== 실제 날씨 기반 추천 완료: {}개 ===", recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("실제 날씨 기반 추천 전체 실패", e);
            return createDefaultRecommendations();
        }
    }

    /**
     * 서울 현재 날씨 조회 (백업용)
     */
    private WeatherResponse getCurrentSeoulWeather() {
        try {
            return weatherService.getCurrentWeather(37.5665, 126.9780); // 서울 좌표
        } catch (Exception e) {
            log.error("서울 날씨 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 특정 관광지의 실제 현재 날씨 조회
     * 각 관광지의 실제 좌표를 사용해 개별 날씨 조회
     */
    private WeatherResponse getCurrentWeatherForAttraction(Attraction attraction) {
        try {
            // 관광지에 좌표가 있는 경우 (실제 위치의 날씨 조회)
            if (attraction.getMapX() != null && attraction.getMapY() != null) {
                double lon = attraction.getMapX().doubleValue(); // X = 경도
                double lat = attraction.getMapY().doubleValue(); // Y = 위도

                log.debug("관광지 {}의 실제 좌표: lat={}, lon={}", attraction.getTitle(), lat, lon);

                // 해당 관광지 위치의 실제 현재 날씨 API 호출
                return weatherService.getCurrentWeather(lat, lon);
            }

            log.debug("관광지 {}의 좌표 없음", attraction.getTitle());
            return null; // 좌표 없으면 null 반환해서 서울 날씨 사용

        } catch (Exception e) {
            log.warn("관광지 {}의 날씨 조회 실패: {}", attraction.getTitle(), e.getMessage());
            return null;
        }
    }

    /**
     * 실제 날씨 데이터로 Response 생성
     * 가짜 온도/습도 대신 API에서 받은 진짜 데이터만 사용
     */
    private MainPageWeatherAttractionResponse createRealWeatherResponse(
            Attraction attraction,
            WeatherResponse realWeather,
            List<String> tags) {

        return MainPageWeatherAttractionResponse.builder()
                .contentId(attraction.getContentId())   // ✅ contentId 추가
                .attractionId(attraction.getAttractionId())
                .attractionName(attraction.getTitle())
                .location(formatAttractionLocation(attraction))
                .description(createWeatherBasedDescription(attraction.getTitle(), realWeather.getWeather()))
                .imageUrl(attraction.getFirstImage() != null ?
                        attraction.getFirstImage() : "/image/creatingRoom/landscape-placeholder-svgrepo-com.svg")

                .weatherType(convertWeatherToKorean(realWeather.getWeather()))
                .weatherIcon(convertWeatherToIcon(realWeather.getWeather()))
                .temperature(Math.round(realWeather.getTemperature()) + "°")
                .dayOfWeek("오늘")
                .openTime(null)
                .tags(tags.isEmpty() ? getDefaultTagsByIndex(0) :
                        tags.subList(0, Math.min(3, tags.size())))
                .build();
    }

    /**
     * 사용 가능한 관광지 ID 조회 (더 많은 관광지 확보)
     */
    private List<Long> getAvailableAttractionIds() {
        try {
            log.info("관광지 조회 시작");

            // 방법 1: 전체 관광지 조회 (limit 제거!)
            List<Attraction> allAttractions = attractionRepository.findAll();
            log.info("전체 관광지 조회 결과: {}개", allAttractions.size());

            if (!allAttractions.isEmpty()) {
                // 모든 관광지 ID 반환 (limit 완전 제거)
                List<Long> allIds = allAttractions.stream()
                        .map(Attraction::getAttractionId)
                        .collect(Collectors.toList());

                log.info("관광지 ID 범위: {} ~ {}",
                        allIds.stream().min(Long::compareTo).orElse(0L),
                        allIds.stream().max(Long::compareTo).orElse(0L));

                return allIds;
            }

            // 방법 2: 감정 기반으로 더 많은 관광지 조회
            log.info("전체 조회 실패, 감정 기반으로 더 많은 관광지 시도");

            // 더 많은 감정 ID로 시도 (기존 10개 → 20개)
            List<Integer> moreEmotionIds = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                    11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
            List<AttractionCardDTO> emotionAttractions = attractionService.findAttractionsByEmotionIds(moreEmotionIds);

            log.info("감정 기반 관광지 조회 결과: {}개", emotionAttractions.size());

            if (!emotionAttractions.isEmpty()) {
                List<Long> emotionBasedIds = emotionAttractions.stream()
                        .map(AttractionCardDTO::getAttractionId)
                        .distinct() // 중복 제거
                        .collect(Collectors.toList());

                log.info("감정 기반 관광지 ID 범위: {} ~ {}",
                        emotionBasedIds.stream().min(Long::compareTo).orElse(0L),
                        emotionBasedIds.stream().max(Long::compareTo).orElse(0L));

                return emotionBasedIds;
            }

            // 방법 3: 페이징으로 더 많은 데이터 확보 시도
            log.info("감정 기반도 실패, 페이징으로 재시도");
            List<Long> pagedIds = new ArrayList<>();

            // 0~50번 ID까지 존재하는지 체크
            for (long id = 1; id <= 50; id++) {
                if (attractionRepository.existsById(id)) {
                    pagedIds.add(id);
                }
            }

            log.info("페이징 기반 조회 결과: {}개 (ID 1~50 범위)", pagedIds.size());

            if (!pagedIds.isEmpty()) {
                return pagedIds;
            }

        } catch (Exception e) {
            log.error("관광지 조회 중 오류: {}", e.getMessage());
        }

        log.warn("모든 관광지 조회 방법 실패");
        return List.of();
    }

    /**
     * 날씨 맞춤 설명 생성
     */
    private String createWeatherBasedDescription(String attractionName, String weather) {
        return switch (weather.toLowerCase()) {
            case "clear" -> attractionName + "에서 맑은 하늘 아래 여유로운 시간을 보내보세요.";
            case "clouds" -> attractionName + "에서 적당히 흐린 날씨 속 운치 있는 분위기를 만끽해보세요.";
            case "rain" -> attractionName + "에서 비 오는 날의 특별한 감성을 느껴보세요.";
            case "snow" -> attractionName + "에서 눈 내리는 겨울 풍경을 감상해보세요.";
            case "thunderstorm" -> attractionName + "에서 뇌우가 지나간 후의 맑은 공기를 느껴보세요.";
            default -> attractionName + "에서 오늘의 날씨와 함께 특별한 시간을 보내보세요.";
        };
    }

    /**
     * Attraction 위치 정보 포맷팅
     */
    private String formatAttractionLocation(Attraction attraction) {
        String addr1 = attraction.getAddr1() != null ? attraction.getAddr1() : "";
        String addr2 = attraction.getAddr2() != null ? attraction.getAddr2() : "";

        if (addr1.isEmpty() && addr2.isEmpty()) {
            return "서울특별시";
        }

        if (addr2.isEmpty()) {
            return addr1;
        } else {
            return addr1 + " " + addr2;
        }
    }

    /**
     * 감정 태그 조회 (안전하게)
     */
    private List<String> getAttractionTags(Long attractionId) {
        try {
            List<String> tags = attractionEmotionService.findEmotionNamesByContentId(attractionId);
            return tags != null && !tags.isEmpty() ? tags : getDefaultTagsByIndex(0);
        } catch (Exception e) {
            log.debug("감정 태그 조회 실패 - attractionId: {}", attractionId);
            return getDefaultTagsByIndex(0);
        }
    }

    /**
     * 영문 날씨를 한글로 변환
     */
    private String convertWeatherToKorean(String weather) {
        if (weather == null) return "맑음";
        return switch (weather.toLowerCase()) {
            case "clear" -> "맑음";
            case "clouds" -> "흐림";
            case "rain" -> "비";
            case "drizzle" -> "이슬비";
            case "thunderstorm" -> "뇌우";
            case "snow" -> "눈";
            case "mist", "fog" -> "안개";
            default -> "맑음";
        };
    }

    /**
     * 날씨를 이모지로 변환
     */
    private String convertWeatherToIcon(String weather) {
        if (weather == null) return "☀️";
        return switch (weather.toLowerCase()) {
            case "clear" -> "☀️";
            case "clouds" -> "☁️";
            case "rain" -> "🌧️";
            case "drizzle" -> "🌦️";
            case "thunderstorm" -> "⛈️";
            case "snow" -> "❄️";
            case "mist", "fog" -> "🌫️";
            default -> "☀️";
        };
    }

    /**
     * 백업 추천 생성 (API 호출 실패 시에만 사용)
     */
    private MainPageWeatherAttractionResponse createBackupRecommendation(int index, String weatherType) {
        String[] attractionNames = {"경복궁", "남산타워", "한강공원"};
        String[] locations = {"서울특별시 종로구", "서울특별시 중구", "서울특별시 마포구"};
        String[] descriptions = {
                "조선 왕조의 역사가 살아 숨 쉬는 아름다운 궁궐",
                "서울의 전경을 한눈에 볼 수 있는 랜드마크",
                "도심 속 자연을 만끽할 수 있는 힐링 스팟"
        };

        return MainPageWeatherAttractionResponse.builder()
                .attractionId((long) (index + 1))
                .attractionName(attractionNames[index % attractionNames.length])
                .location(locations[index % locations.length])
                .description(descriptions[index % descriptions.length])
                .imageUrl("/image/creatingRoom/landscape-placeholder-svgrepo-com.svg")
                .weatherType(convertWeatherToKorean(weatherType))
                .weatherIcon(convertWeatherToIcon(weatherType))
                .temperature("25°") // 백업용만 고정 온도
                .dayOfWeek("오늘")
                .openTime(null)
                .tags(getDefaultTagsByIndex(index))
                .build();
    }

    /**
     * 기본 추천 3개 생성 (완전 실패 시)
     */
    private List<MainPageWeatherAttractionResponse> createDefaultRecommendations() {
        List<MainPageWeatherAttractionResponse> defaults = new ArrayList<>();
        String[] weatherTypes = {"Clear", "Clouds", "Rain"};

        for (int i = 0; i < 3; i++) {
            defaults.add(createBackupRecommendation(i, weatherTypes[i]));
        }

        log.info("기본 추천 3개 생성 완료");
        return defaults;
    }

    /**
     * 기본 태그 반환
     */
    private List<String> getDefaultTagsByIndex(int index) {
        String[][] tagSets = {
                {"역사", "문화", "전통"},
                {"전망", "야경", "랜드마크"},
                {"자연", "힐링", "산책"}
        };
        return List.of(tagSets[index % tagSets.length]);
    }
}