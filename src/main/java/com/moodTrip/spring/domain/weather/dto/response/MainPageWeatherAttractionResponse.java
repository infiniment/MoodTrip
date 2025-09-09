package com.moodTrip.spring.domain.weather.dto.response;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.weather.entity.WeatherAttraction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainPageWeatherAttractionResponse {

    // 관광지 정보
    private Long contentId;
    private Long attractionId;
    private String attractionName;
    private String location;
    private String description;
    private String imageUrl;

    // 날씨 정보
    private String weatherType;      // "맑음", "비", "흐림" 등 (한글)
    private String weatherIcon;      // "☀️", "🌧️", "☁️" 등 (이모지)
    private String temperature;      // "25°" (온도)
    private String dayOfWeek;        // "화", "수", "목" 등
    private String openTime;         // "오전 10:00" (선택사항)

    // 태그 정보
    private List<String> tags;       // ["역사", "문화", "사진"]

    /**
     * WeatherAttraction 엔티티에서 Response DTO로 변환하는 정적 메서드
     * 이 메서드가 핵심입니다 - 데이터베이스 엔티티를 화면용 DTO로 변환
     */
    public static MainPageWeatherAttractionResponse from(WeatherAttraction weatherAttraction, List<String> tags) {
        Attraction attraction = weatherAttraction.getAttraction();

        return MainPageWeatherAttractionResponse.builder()
                .contentId(attraction.getContentId())
                .attractionId(attraction.getAttractionId())
                .attractionName(attraction.getTitle())
                .location(formatLocation(attraction))
                .description(attraction.getTitle())
                .imageUrl(attraction.getFirstImage() != null ? attraction.getFirstImage() : "/image/creatingRoom/landscape-placeholder-svgrepo-com.svg")
                .weatherType(convertWeatherToKorean(weatherAttraction.getWeather()))
                .weatherIcon(convertWeatherToIcon(weatherAttraction.getWeather()))
                .temperature(Math.round(weatherAttraction.getTemperature()) + "°")
                .dayOfWeek(formatDayOfWeek(weatherAttraction.getDateTime()))
                .openTime(null) // 운영시간은 나중에 추가 가능
                .tags(tags)
                .build();
    }

    /**
     * 위치 정보 포맷팅 
     * 예: "서울특별시" + "종로구" -> "서울특별시 종로구"
     */
    private static String formatLocation(Attraction attraction) {
        String addr1 = attraction.getAddr1() != null ? attraction.getAddr1() : "";
        String addr2 = attraction.getAddr2() != null ? attraction.getAddr2() : "";

        if (addr1.isEmpty() && addr2.isEmpty()) {
            return "위치 정보 없음";
        }

        // addr1만 있으면 addr1만, 둘 다 있으면 조합
        if (addr2.isEmpty()) {
            return addr1;
        } else {
            return addr1 + " " + addr2;
        }
    }

    /**
     * 영문 날씨를 한글로 변환
     * API에서 오는 영문 날씨를 사용자가 보기 좋은 한글로 변환
     */
    private static String convertWeatherToKorean(String weather) {
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
     * 시각적으로 날씨를 표현하기 위한 이모지 매핑
     */
    private static String convertWeatherToIcon(String weather) {
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
     * 요일 변환 (LocalDateTime -> 한글 요일)
     * 예: 월요일 -> "월", 화요일 -> "화"
     */
    private static String formatDayOfWeek(LocalDateTime dateTime) {
        if (dateTime == null) return "오늘";

        String[] dayNames = {"월", "화", "수", "목", "금", "토", "일"};
        int dayOfWeek = dateTime.getDayOfWeek().getValue(); // 1=월요일, 7=일요일
        return dayNames[dayOfWeek - 1];
    }
}