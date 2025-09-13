package com.moodTrip.spring.domain.weather.controller;

import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import com.moodTrip.spring.domain.weather.service.WeatherAttractionService;
import com.moodTrip.spring.domain.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class WeatherControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WeatherService weatherService;

    @Mock
    private WeatherAttractionService weatherAttractionService;

    @Mock
    private AttractionService attractionService;

    @InjectMocks
    private WeatherController weatherController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(weatherController).build();
    }

    @Test
    @DisplayName("/api/weather/current/by-coord 호출 시 정상 응답")
    void testGetCurrentByCoord() throws Exception {
        when(weatherService.getCurrentWeather(37.5, 126.9)).thenReturn(new WeatherResponse());

        mockMvc.perform(get("/api/weather/current/by-coord")
                        .param("lat", "37.5")
                        .param("lon", "126.9"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/api/weather/recommend/attractions 호출 시 추천 리스트 반환")
    void testRecommendAttractions() throws Exception {
        when(weatherAttractionService.recommendByCoord(37.5, 126.9))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/weather/recommend/attractions")
                        .param("lat", "37.5")
                        .param("lon", "126.9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("/api/weather/detail 호출 시 Map 응답 구조 확인")
    void testDetailApi() throws Exception {
        when(weatherService.getSeoulCurrentWeather(1L)).thenReturn(new WeatherResponse());
        when(weatherAttractionService.getSeoulAttractionsByWeather(1L))
                .thenReturn(List.of(
                        AttractionResponse.builder()
                                .id(1L)
                                .contentId(123L)
                                .contentTypeId(12)
                                .title("테스트 관광지")
                                .build()
                ));
        when(attractionService.getDetailResponse(1L)).thenReturn(new AttractionDetailResponse());
        when(attractionService.getEmotionTagNames(1L)).thenReturn(List.of("tag1", "tag2"));

        mockMvc.perform(get("/api/weather/detail").param("contentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weather").exists())
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.recommended").isArray());
    }

    @Test
    @DisplayName("/api/weather/hourly 호출 시 리스트 반환")
    void testHourly() throws Exception {
        when(weatherService.getHourlyForecast(eq("2025-09-10"), any(Double.class), any(Double.class)))
                .thenReturn(List.of(new WeatherResponse()));

        mockMvc.perform(get("/api/weather/hourly")
                        .param("date", "2025-09-10")
                        .param("lat", "37.5")
                        .param("lon", "126.9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
