package com.moodTrip.spring.domain.weather.controller;

import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.emotion.service.AttractionEmotionService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WeatherViewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttractionService attractionService;
    @Mock
    private AttractionEmotionService attractionEmotionService;
    @Mock
    private WeatherAttractionService weatherAttractionService;
    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherViewController weatherViewController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(weatherViewController).build();
    }

    @Test
    @DisplayName("/detail 호출 시 뷰 이름과 모델 속성 확인")
    void testDetail() throws Exception {
        Long contentId = 1L;

        when(weatherService.getSeoulCurrentWeather(contentId)).thenReturn(new WeatherResponse());
        when(weatherAttractionService.getSeoulAttractionsByWeather(contentId))
                .thenReturn(List.of(
                        AttractionResponse.builder()
                                .id(1L)
                                .attractionId(10L)
                                .contentId(123L)
                                .contentTypeId(12)
                                .title("테스트 관광지")
                                .addr1("서울 강남구")
                                .areaCode(1)
                                .sigunguCode(2)
                                .build()
                ));
        when(attractionService.getDetailResponse(contentId)).thenReturn(new AttractionDetailResponse());
        when(attractionService.getEmotionTagNames(contentId)).thenReturn(List.of("tag1", "tag2"));

        mockMvc.perform(get("/attraction/weather/detail").param("contentId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("recommand-tourist-attractions-detail/weather-detail-page"))
                .andExpect(model().attributeExists("weather"))
                .andExpect(model().attributeExists("detail"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("recommended"));
    }

}
