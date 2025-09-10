package com.moodTrip.spring.domain.weather.service;

import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository;
import com.moodTrip.spring.domain.emotion.service.AttractionEmotionService;
import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import com.moodTrip.spring.domain.weather.repository.WeatherAttractionRepository;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherAttractionServiceTest {

    @Mock
    private WeatherService weatherService;

    @Mock
    private WeatherEmotionMapper mapper;

    @Mock
    private EmotionRepository emotionRepository;

    @Mock
    private AttractionService attractionService;

    @Mock
    private AttractionEmotionService attractionEmotionService;

    @Mock
    private WeatherAttractionRepository weatherAttractionRepository;

    @InjectMocks
    private WeatherAttractionService weatherAttractionService;

    private WeatherResponse sampleWeather;

    @BeforeEach
    void setUp() {
        sampleWeather = new WeatherResponse();
        sampleWeather.setWeather("Clear");
        sampleWeather.setTemperature(25.0);
    }

    @Test
    @DisplayName("recommendByCoord: 날씨 기반 추천 반환")
    void testRecommendByCoord() {
        when(weatherService.getCurrentWeather(anyDouble(), anyDouble()))
                .thenReturn(sampleWeather);

        List<AttractionCardDTO> result = weatherAttractionService.recommendByCoord(37.5, 126.9);

        assertThat(result).isNotNull();
    }
}
