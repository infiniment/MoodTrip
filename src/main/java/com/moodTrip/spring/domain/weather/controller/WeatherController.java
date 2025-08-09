package com.moodTrip.spring.domain.weather.controller;

import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import com.moodTrip.spring.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/daily")
    public List<WeatherResponse> getDailyForecast() {
        return weatherService.getDailyForecast();
    }

    @GetMapping("/hourly")
    public List<WeatherResponse> getHourlyForecast(@RequestParam String date) {
        return weatherService.getHourlyForecast(date);
    }

    @GetMapping("/current")
    public WeatherResponse getCurrentWeather() {
        return weatherService.getCurrentWeather();

    }
}
