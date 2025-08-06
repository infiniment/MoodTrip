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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

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

    @Override
    public List<WeatherResponse> getDailyForecast() {
        JsonNode root = callApi();
        Map<String, List<WeatherResponse>> grouped = new HashMap<>();
        List<Weather> weatherEntities = new ArrayList<>();

        for (JsonNode node : root.get("list")) {
            String dtTxt = node.get("dt_txt").asText();
            String date = dtTxt.split(" ")[0];
            String time = dtTxt.split(" ")[1];

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

        weatherRepository.saveAll(weatherEntities);

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // 날짜 기준 정렬
                .map(entry -> {
                    List<WeatherResponse> list = entry.getValue();
                    WeatherResponse first = list.get(0);

                    double maxTemp = list.stream().mapToDouble(WeatherResponse::getTemperature).max().orElse(0);
                    double minTemp = list.stream().mapToDouble(WeatherResponse::getTemperature).min().orElse(0);

                    first.setMaxTemp(maxTemp);
                    first.setMinTemp(minTemp);

                    return first;
                })
                .collect(Collectors.toList());
    }

    @Override
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

        weatherRepository.saveAll(weatherEntities);

        return hourlyList;
    }

    @Override
    public WeatherResponse getCurrentWeather() {
        JsonNode root = callApi();
        JsonNode node = root.get("list").get(0);

        String dtTxt = node.get("dt_txt").asText(); // 첫 번째 시간대의 데이터
        String date = dtTxt.split(" ")[0];
        String time = dtTxt.split(" ")[1];

        if (!root.has("list") || root.get("list").isEmpty()) {
            throw new IllegalStateException("날씨 예보 리스트가 비어 있습니다.");
        }

        Weather entity = Weather.builder()
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

        return new WeatherResponse(entity);
    }
}
