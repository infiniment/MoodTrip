package com.moodTrip.spring.domain.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import com.moodTrip.spring.domain.weather.entity.Weather;
import com.moodTrip.spring.domain.weather.entity.WeatherAttraction;
import com.moodTrip.spring.domain.weather.repository.WeatherAttractionRepository;
import com.moodTrip.spring.domain.weather.repository.WeatherRepository;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock private WeatherRepository weatherRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private WeatherAttractionRepository weatherAttractionRepository;
    @Mock private AttractionService attractionService;

    @InjectMocks
    private WeatherServiceImpl weatherServiceReal;

    private WeatherServiceImpl weatherService; // Spy

    private Room sampleRoom;
    private Weather sampleWeather;

    @BeforeEach
    void setUp() {
        // Spy 생성 (실제 로직 + 일부만 stub)
        weatherService = org.mockito.Mockito.spy(weatherServiceReal);

        sampleRoom = Room.builder()
                .roomId(1L)
                .destinationLat(BigDecimal.valueOf(37.5))
                .destinationLon(BigDecimal.valueOf(126.9))
                .attraction(Attraction.builder().attractionId(10L).build())
                .build();

        sampleWeather = Weather.builder()
                .dateTime(LocalDateTime.of(2025, 9, 10, 12, 0))
                .date("2025-09-10")
                .time("12:00:00")
                .temperature(25.0)
                .feelsLike(26.0)
                .humidity(60)
                .weather("Clear")
                .description("맑음")
                .icon("01d")
                .lat(37.5)
                .lon(126.9)
                .room(sampleRoom)
                .attraction(sampleRoom.getAttraction())
                .build();
    }

    @Test
    @DisplayName("getDailyByRoom: roomId 기반 예보 조회 성공 (API stub)")
    void testGetDailyByRoom_withStubbedApi() throws Exception {
        when(roomRepository.findWithAttractionByRoomId(1L)).thenReturn(Optional.of(sampleRoom));

        String fakeJson = """
                {
                  "list": [
                    {
                      "dt_txt": "2025-09-10 12:00:00",
                      "main": { "temp": 25.0, "feels_like": 26.0, "humidity": 60 },
                      "weather": [ { "main": "Clear", "description": "맑음", "icon": "01d" } ]
                    }
                  ]
                }
                """;
        JsonNode root = new ObjectMapper().readTree(fakeJson);
        doReturn(root).when(weatherService).callApi(anyDouble(), anyDouble());

        when(weatherRepository.saveAll(anyList())).thenReturn(List.of(sampleWeather));

        List<WeatherResponse> result = weatherService.getDailyByRoom(1L);

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get(0).getWeather()).isEqualTo("Clear");
        assertThat(result.get(0).getTemperature()).isEqualTo(25.0);
    }

    @Test
    @DisplayName("getHourlyByRoom: 특정 날짜의 시간별 예보 조회 성공 (API stub)")
    void testGetHourlyByRoom_withStubbedApi() throws Exception {
        when(roomRepository.findWithAttractionByRoomId(1L)).thenReturn(Optional.of(sampleRoom));
        when(weatherRepository.findByRoom_RoomIdAndDateOrderByTimeAsc(1L, "2025-09-10"))
                .thenReturn(List.of()); // 캐시 없음

        String fakeJson = """
                {
                  "list": [
                    {
                      "dt_txt": "2025-09-10 09:00:00",
                      "main": { "temp": 22.0, "feels_like": 22.5, "humidity": 55 },
                      "weather": [ { "main": "Clouds", "description": "흐림", "icon": "02d" } ]
                    },
                    {
                      "dt_txt": "2025-09-10 12:00:00",
                      "main": { "temp": 25.0, "feels_like": 26.0, "humidity": 60 },
                      "weather": [ { "main": "Clear", "description": "맑음", "icon": "01d" } ]
                    }
                  ]
                }
                """;
        JsonNode root = new ObjectMapper().readTree(fakeJson);
        doReturn(root).when(weatherService).callApi(anyDouble(), anyDouble());

        when(weatherRepository.saveAll(anyList())).thenReturn(List.of(sampleWeather));

        List<WeatherResponse> result = weatherService.getHourlyByRoom(1L, "2025-09-10");

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result.get(0).getDate()).isEqualTo("2025-09-10");
    }

    @Test
    @DisplayName("getCurrentByRoom: 현재 날씨 조회 성공 (API stub)")
    void testGetCurrentByRoom_withStubbedApi() throws Exception {
        when(roomRepository.findWithAttractionByRoomId(1L)).thenReturn(Optional.of(sampleRoom));

        String fakeJson = """
                {
                  "list": [
                    {
                      "dt_txt": "2025-09-10 12:00:00",
                      "main": { "temp": 28.0, "feels_like": 29.0, "humidity": 70 },
                      "weather": [ { "main": "Clouds", "description": "흐림", "icon": "02d" } ]
                    }
                  ]
                }
                """;
        JsonNode root = new ObjectMapper().readTree(fakeJson);
        doReturn(root).when(weatherService).callApi(anyDouble(), anyDouble());

        when(weatherRepository.saveAll(anyList())).thenReturn(List.of(sampleWeather));

        WeatherResponse response = weatherService.getCurrentByRoom(1L);

        assertThat(response).isNotNull();
        assertThat(response.getWeather()).isEqualTo("Clear"); // sampleWeather 기준
    }

    @Test
    @DisplayName("getSeoulCurrentWeather: contentId 기반 현재 날씨 조회 성공 (API stub)")
    void testGetSeoulCurrentWeather_withStubbedApi() throws Exception {
        Long contentId = 99L;

        // AttractionService → Attraction mock
        Attraction attraction = Attraction.builder().attractionId(contentId).build();
        when(attractionService.getEntityByContentId(contentId)).thenReturn(attraction);

        // WeatherAttractionRepository 캐시 조회 → 빈 값
        when(weatherAttractionRepository.findTopByAttraction_AttractionIdAndDateTimeGreaterThanEqualOrderByDateTimeDesc(
                anyLong(), any(LocalDateTime.class))
        ).thenReturn(Optional.empty());

        // callOpenWeather → fake JSON stub
        String fakeJson = """
                {
                  "list": [
                    {
                      "dt": 1757505600,
                      "main": { "temp": 20.0, "feels_like": 19.0, "humidity": 50 },
                      "weather": [ { "main": "Rain", "description": "비", "icon": "10d" } ]
                    }
                  ]
                }
                """;
        JsonNode root = new ObjectMapper().readTree(fakeJson);
        doReturn(root).when(weatherService).callOpenWeather(anyDouble(), anyDouble());

        // WeatherAttractionRepository save → 저장 시 그대로 리턴
        when(weatherAttractionRepository.save(any(WeatherAttraction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        WeatherResponse response = weatherService.getSeoulCurrentWeather(contentId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getWeather()).isEqualTo("Rain");
        assertThat(response.getTemperature()).isEqualTo(20.0);
    }
}
