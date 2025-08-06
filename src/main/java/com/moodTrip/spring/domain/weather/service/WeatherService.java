package com.moodTrip.spring.domain.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import com.moodTrip.spring.domain.weather.entity.Weather;
import com.moodTrip.spring.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherRepository weatherRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${weather.api.key}")
    private String apiKey;

    private final double lat = 37.5665;
    private final double lon = 126.9780;

    private JsonNode callApi() {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&units=metric&lang=kr&appid=%s",
                lat, lon, apiKey
        );

        try {
            String json = restTemplate.getForObject(url, String.class);
            return new ObjectMapper().readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("날씨 API 호출 실패", e);
        }
    }

    public List<WeatherResponse> getDailyForecast() {
        JsonNode root = callApi();
        Map<String, List<WeatherResponse>> grouped = new HashMap<>();
        List<Weather> weatherEntities = new ArrayList<>();

        for (JsonNode node : root.get("list")) {
            String dtTxt = node.get("dt_txt").asText();  // "2025-08-06 09:00:00"
            String date = dtTxt.split(" ")[0];
            String time = dtTxt.split(" ")[1];

            // WeatherResponse 만들기
            WeatherResponse wr = WeatherResponse.builder()
                    .dateTime(dtTxt)
                    .date(date)
                    .time(time)
                    .temperature(node.get("main").get("temp").asDouble())
                    .feelsLike(node.get("main").get("feels_like").asDouble())
                    .humidity(node.get("main").get("humidity").asInt())
                    .weather(node.get("weather").get(0).get("main").asText())
                    .description(node.get("weather").get(0).get("description").asText())
                    .icon(node.get("weather").get(0).get("icon").asText())
                    .lat(lat)
                    .lon(lon)
                    .build();

            // ✅ DB 저장용 Entity 생성
            Weather entity = Weather.builder()
                    .dateTime(dtTxt)
                    .date(date)
                    .time(time)
                    .temperature(wr.getTemperature())
                    .feelsLike(wr.getFeelsLike())
                    .humidity(wr.getHumidity())
                    .weather(wr.getWeather())
                    .description(wr.getDescription())
                    .icon(wr.getIcon())
                    .lat(lat)
                    .lon(lon)
                    .build();

            weatherEntities.add(entity);
            grouped.computeIfAbsent(date, k -> new ArrayList<>()).add(wr);
        }

        // ✅ 저장
        weatherRepository.saveAll(weatherEntities);

        return grouped.values().stream().map(list -> list.get(0)).toList();
    }


    public List<WeatherResponse> getHourlyForecast(String dateStr) {
        JsonNode root = callApi();

        List<WeatherResponse> hourlyList = new ArrayList<>();
        List<Weather> weatherEntities = new ArrayList<>();

        for (JsonNode node : root.get("list")) {
            String dtTxt = node.get("dt_txt").asText();
            String date = dtTxt.split(" ")[0];
            String time = dtTxt.split(" ")[1];

            if (date.equals(dateStr)) {
                WeatherResponse wr = WeatherResponse.builder()
                        .dateTime(dtTxt)
                        .date(date)
                        .time(time)
                        .temperature(node.get("main").get("temp").asDouble())
                        .feelsLike(node.get("main").get("feels_like").asDouble())
                        .humidity(node.get("main").get("humidity").asInt())
                        .weather(node.get("weather").get(0).get("main").asText())
                        .description(node.get("weather").get(0).get("description").asText())
                        .icon(node.get("weather").get(0).get("icon").asText())
                        .lat(lat)
                        .lon(lon)
                        .build();

                // ✅ Weather entity 만들기
                Weather entity = Weather.builder()
                        .dateTime(dtTxt)
                        .date(date)
                        .time(time)
                        .temperature(wr.getTemperature())
                        .feelsLike(wr.getFeelsLike())
                        .humidity(wr.getHumidity())
                        .weather(wr.getWeather())
                        .description(wr.getDescription())
                        .icon(wr.getIcon())
                        .lat(lat)
                        .lon(lon)
                        .build();

                weatherEntities.add(entity);
                hourlyList.add(wr);
            }
        }

        // ✅ DB에 저장
        weatherRepository.saveAll(weatherEntities);

        return hourlyList;
    }

}
