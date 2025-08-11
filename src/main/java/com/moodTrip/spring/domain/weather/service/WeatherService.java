package com.moodTrip.spring.domain.weather.service;

import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;

import java.util.List;

public interface WeatherService {

    // 3일 예보
    List<WeatherResponse> getDailyForecast(double lat, double lon);

    // 특정 날짜 시간별 예보
    List<WeatherResponse> getHourlyForecast(String date, double lat, double lon);

    //현재 날씨
    WeatherResponse getCurrentWeather(double lat, double lon);


    // ★ roomId 기반(오버로드)
    List<WeatherResponse> getDailyByRoom(Long roomId);
    List<WeatherResponse> getHourlyByRoom(Long roomId, String date);
    WeatherResponse getCurrentByRoom(Long roomId);
}