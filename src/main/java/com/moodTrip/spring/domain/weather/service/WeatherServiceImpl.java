package com.moodTrip.spring.domain.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import com.moodTrip.spring.domain.weather.entity.Weather;
import com.moodTrip.spring.domain.weather.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final RoomRepository roomRepository;

    private double toDouble(BigDecimal bd, String field) {
        if (bd == null) throw new IllegalStateException(field + " is null");
        return bd.setScale(7, RoundingMode.HALF_UP).doubleValue();
    }

    private Room getRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found: " + roomId));
    }

    @Value("${weather.api.key}")
    private String apiKey;

    private JsonNode callApi(double lat, double lon) {
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
    public List<WeatherResponse> getDailyForecast(double lat, double lon) {
        JsonNode root = callApi(lat, lon);
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

//        weatherRepository.saveAll(weatherEntities);

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
    public List<WeatherResponse> getHourlyForecast(String dateStr, double lat, double lon) {
        JsonNode root = callApi(lat, lon);

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

//        weatherRepository.saveAll(weatherEntities);

        return hourlyList;
    }

    @Override
    public WeatherResponse getCurrentWeather(double lat, double lon) {
        JsonNode root = callApi(lat, lon);
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

    // ===== roomId 기반 오버로드: 여기서 좌표 추출 후 위임 =====

    private List<Weather> saveForecast(JsonNode root, Room room, double lat, double lon) {
        List<Weather> batch = new ArrayList<>();
        for (JsonNode node : root.get("list")) {
            String dtTxt = node.get("dt_txt").asText();
            String[] p = dtTxt.split(" ");
            batch.add(Weather.builder()
                    .dateTime(dtTxt).date(p[0]).time(p[1])
                    .temperature(node.get("main").get("temp").asDouble())
                    .feelsLike(node.get("main").get("feels_like").asDouble())
                    .humidity(node.get("main").get("humidity").asInt())
                    .weather(node.get("weather").get(0).get("main").asText())
                    .description(node.get("weather").get(0).get("description").asText())
                    .icon(node.get("weather").get(0).get("icon").asText())
                    .lat(lat).lon(lon)
                    .room(room)                 // ✅ 반드시 세팅
                    .build());
        }
        return weatherRepository.saveAll(batch);
    }

    @Override
    public List<WeatherResponse> getDailyByRoom(Long roomId) {
        Room room = getRoom(roomId);
        double lat = toDouble(room.getDestinationLat(), "destinationLat");
        double lon = toDouble(room.getDestinationLon(), "destinationLon");

        JsonNode root = callApi(lat, lon);
        List<Weather> saved = saveForecast(root, room, lat, lon);

        Map<String, List<Weather>> byDate = saved.stream().collect(Collectors.groupingBy(Weather::getDate));
        return byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Weather first = e.getValue().get(0);
                    double maxT = e.getValue().stream().mapToDouble(Weather::getTemperature).max().orElse(0);
                    double minT = e.getValue().stream().mapToDouble(Weather::getTemperature).min().orElse(0);
                    WeatherResponse wr = new WeatherResponse(first);
                    wr.setMaxTemp(maxT);
                    wr.setMinTemp(minT);
                    return wr;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<WeatherResponse> getHourlyByRoom(Long roomId, String date) {
        Room room = getRoom(roomId);
        double lat = toDouble(room.getDestinationLat(), "destinationLat");
        double lon = toDouble(room.getDestinationLon(), "destinationLon");

        // 캐시/DB 우선 원하면 여기서 조회하고 비면 저장
        JsonNode root = callApi(lat, lon);
        saveForecast(root, room, lat, lon);

        // 저장된 것에서 필요한 날짜만 꺼내기
        return weatherRepository.findByRoom_RoomIdAndDateOrderByTimeAsc(roomId, date)
                .stream().map(WeatherResponse::new).collect(Collectors.toList());
    }

    @Override
    public WeatherResponse getCurrentByRoom(Long roomId) {
        Room room = getRoom(roomId);
        double lat = toDouble(room.getDestinationLat(), "destinationLat");
        double lon = toDouble(room.getDestinationLon(), "destinationLon");

        JsonNode root = callApi(lat, lon);
        List<Weather> saved = saveForecast(root, room, lat, lon);
        return new WeatherResponse(saved.get(0));
    }
}
