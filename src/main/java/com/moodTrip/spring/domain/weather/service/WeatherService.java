package com.moodTrip.spring.domain.weather.service;

import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;

import java.util.List;

public interface WeatherService {

    // 3일 예보
    List<WeatherResponse> getDailyForecast();

    // 특정 날짜 시간별 예보
    List<WeatherResponse> getHourlyForecast(String dateStr);

    //현재 날씨
    WeatherResponse getCurrentWeather();
}