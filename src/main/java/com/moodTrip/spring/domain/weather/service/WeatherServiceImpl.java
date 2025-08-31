package com.moodTrip.spring.domain.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import com.moodTrip.spring.domain.weather.entity.Weather;
import com.moodTrip.spring.domain.weather.entity.WeatherAttraction;
import com.moodTrip.spring.domain.weather.repository.WeatherAttractionRepository;
import com.moodTrip.spring.domain.weather.repository.WeatherRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.hibernate.cfg.JdbcSettings.URL;
import static org.springframework.data.redis.core.RedisCommand.TTL;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final RoomRepository roomRepository;
    private final AttractionRepository attractionRepository;
    private final AttractionService attractionService;
    private final WeatherAttractionRepository  weatherAttractionRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private double toDouble(BigDecimal bd, String field) {
        if (bd == null) {
            log.error("[weather] {} is null on room", field);
            throw new IllegalStateException(field + " is null");
        }
        return bd.setScale(7, RoundingMode.HALF_UP).doubleValue();
    }

    private Room getRoom(Long roomId) {
        return roomRepository.findWithAttractionByRoomId(roomId)
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
        } catch (org.springframework.web.client.RestClientResponseException e) {
            // HTTP 상태와 바디까지 로그
            log.error("[weather] API {} failed: status={}, body={}", url, e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("날씨 API 호출 실패: " + e.getRawStatusCode(), e);
        } catch (Exception e) {
            log.error("[weather] API {} failed", url, e);
            throw new RuntimeException("날씨 API 호출 실패", e);
        }
    }

    @Override
    public List<WeatherResponse> getDailyForecast(double lat, double lon) {
        JsonNode root = callApi(lat, lon);
        JsonNode forecastArray = root.get("list");
        if (forecastArray == null || !forecastArray.isArray()) return List.of();

        Map<String, List<WeatherResponse>> grouped = new HashMap<>();
        List<Weather> weatherEntities = new ArrayList<>();

        for (JsonNode node : forecastArray) {
            String dtTxt = node.get("dt_txt").asText();
            LocalDateTime dt = LocalDateTime.parse(dtTxt, formatter);
            String date = dtTxt.split(" ")[0];
            String time = dtTxt.split(" ")[1];

            WeatherResponse wr = WeatherResponse.builder()
                    .dateTime(dt.format(formatter))
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
                    .dateTime(dt)
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

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<WeatherResponse> dayForecasts = entry.getValue();
                    WeatherResponse first = dayForecasts.get(0);

                    double maxTemp = dayForecasts.stream().mapToDouble(WeatherResponse::getTemperature).max().orElse(0);
                    double minTemp = dayForecasts.stream().mapToDouble(WeatherResponse::getTemperature).min().orElse(0);

                    first.setMaxTemp(maxTemp);
                    first.setMinTemp(minTemp);

                    return first;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<WeatherResponse> getHourlyForecast(String dateStr, double lat, double lon) {
        JsonNode root = callApi(lat, lon);
        JsonNode list = root.get("list");
        if (list == null || !list.isArray()) return List.of();

        List<WeatherResponse> hourlyList = new ArrayList<>();
        List<Weather> weatherEntities = new ArrayList<>();

        for (JsonNode node : list) {
            String dtTxt = node.get("dt_txt").asText();
            LocalDateTime dt = LocalDateTime.parse(dtTxt, formatter);
            String date = dtTxt.split(" ")[0];
            String time = dtTxt.split(" ")[1];

            if (date.equals(dateStr)) {
                WeatherResponse wr = WeatherResponse.builder()
                        .dateTime(dt.format(formatter))
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
                        .dateTime(dt)
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
        if (!root.has("list") || !root.get("list").isArray() || root.get("list").isEmpty()) {
            throw new IllegalStateException("날씨 예보 리스트가 비어 있습니다.");
        }

        JsonNode node = root.get("list").get(0);


        String dtTxt = node.get("dt_txt").asText();
        LocalDateTime dt = LocalDateTime.parse(dtTxt, formatter);
        String date = dtTxt.split(" ")[0];
        String time = dtTxt.split(" ")[1];

        if (!root.has("list") || root.get("list").isEmpty()) {
            throw new IllegalStateException("날씨 예보 리스트가 비어 있습니다.");
        }

        Weather entity = Weather.builder()
                .dateTime(dt)
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
        var attraction = room.getAttraction();
        if (attraction == null) {
            throw new IllegalStateException("Room(" + room.getRoomId() + ") has no Attraction bound.");
        }
        if (!root.has("list") || !root.get("list").isArray()) return List.of();

        // 이미 저장된 dateTime(중복 방지)
        Set<LocalDateTime> existing = weatherRepository.findAllDateTimesByRoomId(room.getRoomId());

        log.info("[weather] existing dateTimes: {}", existing);

        List<Weather> batch = new ArrayList<>();
        for (JsonNode node : root.get("list")) {
            String dtTxtRaw = node.get("dt_txt").asText();
            LocalDateTime dt = LocalDateTime.parse(dtTxtRaw, formatter); // 문자열을 LocalDateTime으로 파싱

            if (existing.contains(dt)) {   // LocalDateTime 그대로 비교
                log.info("[weather] 중복된 데이터 건너뜀: {}", dt);
                continue;
            }

            String[] p = dtTxtRaw.split(" ");

            batch.add(Weather.builder()
                    .dateTime(dt).date(p[0]).time(p[1])
                    .temperature(node.get("main").get("temp").asDouble())
                    .feelsLike(node.get("main").get("feels_like").asDouble())
                    .humidity(node.get("main").get("humidity").asInt())
                    .weather(node.get("weather").get(0).get("main").asText())
                    .description(node.get("weather").get(0).get("description").asText())
                    .icon(node.get("weather").get(0).get("icon").asText())
                    .lat(lat).lon(lon)
                    .room(room)
                    .attraction(attraction)
                    .build());
        }
        return batch.isEmpty() ? List.of() : weatherRepository.saveAll(batch);
    }

    @Override
    public List<WeatherResponse> getDailyByRoom(Long roomId) {
        Room room = getRoom(roomId);
        double lat = toDouble(room.getDestinationLat(), "destinationLat");
        double lon = toDouble(room.getDestinationLon(), "destinationLon");

        JsonNode root = callApi(lat, lon);
        List<Weather> saved = saveForecast(root, room, lat, lon);

        // 저장된 게 없으면(이미 캐시되어 있으면) 레포에서 기간 조회로 만들어 주기
        if (saved.isEmpty()) {
            JsonNode list = root.get("list");
            if (list == null || !list.isArray() || list.isEmpty()) return List.of();

            String firstDate = list.get(0).get("dt_txt").asText().split(" ")[0];
            String lastDate = list.get(list.size() - 1).get("dt_txt").asText().split(" ")[0];

            List<Weather> cachedRange =
                    weatherRepository.findByRoom_RoomIdAndDateBetweenOrderByDateAscTimeAsc(roomId, firstDate, lastDate);

            Map<String, List<Weather>> byDate = cachedRange.stream()
                    .collect(Collectors.groupingBy(Weather::getDate));

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

        // 방금 저장한 saved로 요약
        Map<String, List<Weather>> byDate = saved.stream()
                .collect(Collectors.groupingBy(Weather::getDate));

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
        List<Weather> cached = weatherRepository.findByRoom_RoomIdAndDateOrderByTimeAsc(roomId, date);
        if (!cached.isEmpty()) return cached.stream().map(WeatherResponse::new).toList();

        Room room = getRoom(roomId);
        double lat = toDouble(room.getDestinationLat(), "destinationLat");
        double lon = toDouble(room.getDestinationLon(), "destinationLon");

        JsonNode root = callApi(lat, lon);
        saveForecast(root, room, lat, lon);
        return weatherRepository.findByRoom_RoomIdAndDateOrderByTimeAsc(roomId, date)
                .stream().map(WeatherResponse::new).toList();
    }

    @Override
    public WeatherResponse getCurrentByRoom(Long roomId) {
        Room room = getRoom(roomId);
        double lat = toDouble(room.getDestinationLat(), "destinationLat");
        double lon = toDouble(room.getDestinationLon(), "destinationLon");

        JsonNode root = callApi(lat, lon);
        List<Weather> saved = saveForecast(root, room, lat, lon);

        if (!saved.isEmpty()) {
            return new WeatherResponse(saved.get(0));
        }

        // 1) DB에 기존 캐시가 있으면 그걸 사용
        var cached = weatherRepository.findTopByRoom_RoomIdOrderByDateTimeAsc(roomId);
        if (cached.isPresent()) return new WeatherResponse(cached.get());

        // 2) 그래도 없으면 API 첫 요소로 DTO 만들어 반환(저장까지는 생략 가능)
        JsonNode list = root.get("list");
        if (list != null && list.isArray() && !list.isEmpty()) {
            JsonNode node = list.get(0);
            String dtTxt = node.get("dt_txt").asText();
            String[] p = dtTxt.split(" ");
            return WeatherResponse.builder()
                    .dateTime(dtTxt)
                    .date(p[0])
                    .time(p[1])
                    .temperature(node.get("main").get("temp").asDouble())
                    .feelsLike(node.get("main").get("feels_like").asDouble())
                    .humidity(node.get("main").get("humidity").asInt())
                    .weather(node.get("weather").get(0).get("main").asText())
                    .description(node.get("weather").get(0).get("description").asText())
                    .icon(node.get("weather").get(0).get("icon").asText())
                    .lat(lat)
                    .lon(lon)
                    .build();
        }

        throw new IllegalStateException("날씨 예보 리스트가 비어 있습니다.");
    }

    // 캐시 유효기간 (30분)
    private static final Duration TTL = Duration.ofMinutes(60);
    private static final double SEOUL_LAT = 37.5665;
    private static final double SEOUL_LON = 126.9780;
    private static final long DEFAULT_ROOM_ID = 1L;

    // @Transactional  // ⛔ 이 줄 삭제
    private WeatherAttraction upsertWeatherAttraction(WeatherAttraction entity) {
        Long attractionId = entity.getAttraction().getAttractionId();
        LocalDateTime dt = entity.getDateTime();

        var found = weatherAttractionRepository
                .findByAttraction_AttractionIdAndDateTime(attractionId, dt);
        if (found.isPresent()) return found.get();

        try {
            return weatherAttractionRepository.save(entity);
        } catch (DataIntegrityViolationException dup) {
            return weatherAttractionRepository
                    .findByAttraction_AttractionIdAndDateTime(attractionId, dt)
                    .orElseThrow(() -> dup);
        }
    }

    @Override
    public WeatherResponse getSeoulCurrentWeather(Long contentId) {
        // 1) 캐시(최근 30분 이내)가 있으면 재사용
        var thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        var cached = weatherAttractionRepository
                .findTopByAttraction_AttractionIdAndDateTimeGreaterThanEqualOrderByDateTimeDesc(
                        contentId, thirtyMinutesAgo
                );
        if (cached.isPresent()) {
            return new WeatherResponse(cached.get());
        }

        // 2) API 호출 (서울 고정)
        var root = callOpenWeather(SEOUL_LAT, SEOUL_LON);

        // 3) 파싱(가장 가까운 1개만 저장)
        var list = root.path("list");
        if (!list.isArray() || list.size() == 0) {
            throw new IllegalStateException("OpenWeather 응답에 list가 없습니다.");
        }

        var first = list.get(0);
        var dt = first.get("dt").asLong(); // unix
        var main = first.get("main");
        var weather0 = first.withArray("weather").get(0);

        LocalDateTime dtLocal = LocalDateTime.ofEpochSecond(dt, 0, java.time.ZoneOffset.ofHours(9));
        String date = dtLocal.toLocalDate().toString();
        String time = dtLocal.toLocalTime().withNano(0).toString();

        var attraction = attractionService.getEntityByContentId(contentId);

        WeatherAttraction toSave = WeatherAttraction.builder()
                .dateTime(dtLocal)
                .date(date)
                .time(time)
                .temperature(main.get("temp").asDouble())
                .feelsLike(main.get("feels_like").asDouble())
                .humidity(main.get("humidity").asInt())
                .weather(weather0.get("main").asText(null))
                .description(weather0.get("description").asText(null))
                .icon(weather0.get("icon").asText(null))
                .lat(SEOUL_LAT)
                .lon(SEOUL_LON)
                .attraction(attraction)
                .build();

        WeatherAttraction saved = upsertWeatherAttraction(toSave);
        return new WeatherResponse(saved);
    }

    // 공용 API 호출(네가 기존에 쓰던 것 그대로 재사용)
    private com.fasterxml.jackson.databind.JsonNode callOpenWeather(double lat, double lon) {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&units=metric&lang=kr&appid=%s",
                lat, lon, apiKey
        );
        try {
            String json = restTemplate.getForObject(url, String.class);
            return new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
        } catch (org.springframework.web.client.RestClientResponseException e) {
            log.error("[weather] API {} failed: status={}, body={}", url, e.getRawStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("날씨 API 호출 실패: " + e.getRawStatusCode(), e);
        } catch (Exception e) {
            log.error("[weather] API {} failed", url, e);
            throw new RuntimeException("날씨 API 호출 실패", e);
        }
    }


}
