package com.moodTrip.spring.domain.weather.controller;

import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import com.moodTrip.spring.domain.weather.service.WeatherAttractionService;
import com.moodTrip.spring.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private final RoomRepository roomRepository; // ★ 임시로 주입
    private final WeatherAttractionService weatherAttractionService;
    private final AttractionService attractionService;

    private static final double DEFAULT_LAT = 37.5665;
    private static final double DEFAULT_LON = 126.9780;
//
//    private double resolve(Double v, double def) { return v != null ? v : def; }
//
//    @GetMapping("/daily")
//    public List<WeatherResponse> getDailyForecast(@RequestParam(required = false) Double lat,
//                                                  @RequestParam(required = false) Double lon) {
//        return weatherService.getDailyForecast(resolve(lat, DEFAULT_LAT), resolve(lon, DEFAULT_LON));
//    }
//
//    @GetMapping("/hourly")
//    public List<WeatherResponse> getHourlyForecast(@RequestParam String date,
//                                                   @RequestParam(required = false) Double lat,
//                                                   @RequestParam(required = false) Double lon) {
//        return weatherService.getHourlyForecast(date, resolve(lat, DEFAULT_LAT), resolve(lon, DEFAULT_LON));
//    }
//
//    @GetMapping("/current")
//    public WeatherResponse getCurrentWeather(@RequestParam(required = false) Double lat,
//                                             @RequestParam(required = false) Double lon) {
//        return weatherService.getCurrentWeather(resolve(lat, DEFAULT_LAT), resolve(lon, DEFAULT_LON));
//    }

    //===================임시==========================
    private double[] resolveLatLon(Long roomId, Double lat, Double lon) {
        if (roomId != null) {
            var room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new NoSuchElementException("Room not found: " + roomId));
            double rlat = room.getDestinationLat() != null ? room.getDestinationLat().doubleValue() : DEFAULT_LAT;
            double rlon = room.getDestinationLon() != null ? room.getDestinationLon().doubleValue() : DEFAULT_LON;
            return new double[]{rlat, rlon};
        }
        return new double[]{
                lat != null ? lat : DEFAULT_LAT,
                lon != null ? lon : DEFAULT_LON
        };
    }

    @GetMapping("/daily")
    public List<WeatherResponse> daily(
            @RequestParam(name="roomId", required=false) Long roomId,
            @RequestParam(name="lat", required=false) Double lat,
            @RequestParam(name="lon", required=false) Double lon
    ) {
        if (roomId != null) return weatherService.getDailyByRoom(roomId);   // ★
        double la = (lat != null ? lat : 37.5665);
        double lo = (lon != null ? lon : 126.9780);
        return weatherService.getDailyForecast(la, lo);                      // 좌표 기반
    }

    @GetMapping("/current")
    public WeatherResponse current(
            @RequestParam(name="roomId", required=false) Long roomId,
            @RequestParam(name="lat", required=false) Double lat,
            @RequestParam(name="lon", required=false) Double lon
    ) {
        if (roomId != null) return weatherService.getCurrentByRoom(roomId);  // ★
        double la = (lat != null ? lat : 37.5665);
        double lo = (lon != null ? lon : 126.9780);
        return weatherService.getCurrentWeather(la, lo);
    }

    @GetMapping("/hourly")
    public List<WeatherResponse> hourly(
            @RequestParam(name="date") String date,
            @RequestParam(name="roomId", required=false) Long roomId,
            @RequestParam(name="lat", required=false) Double lat,
            @RequestParam(name="lon", required=false) Double lon
    ) {
        if (roomId != null) return weatherService.getHourlyByRoom(roomId, date); // ★
        double la = (lat != null ? lat : 37.5665);
        double lo = (lon != null ? lon : 126.9780);
        return weatherService.getHourlyForecast(date, la, lo);
    }

    // 필요하면 좌표 기반 엔드포인트도 유지
    @GetMapping("/current/by-coord")
    public WeatherResponse getCurrentByCoord(@RequestParam double lat, @RequestParam double lon) {
        return weatherService.getCurrentWeather(lat, lon);
    }

    @GetMapping("/recommend/attractions")
    public List<AttractionCardDTO> recommend(@RequestParam Double lat, @RequestParam Double lon) {
        return weatherAttractionService.recommendByCoord(lat, lon);
    }


    @GetMapping("/detail")
    public Map<String, Object> detailApi(@RequestParam("contentId") Long contentId) {
        WeatherResponse weather = weatherService.getSeoulCurrentWeather(contentId);
        List<AttractionResponse> recommended = weatherAttractionService.getSeoulAttractionsByWeather(contentId);
        AttractionDetailResponse detail = attractionService.getDetailResponse(contentId);
        List<String> tags = attractionService.getEmotionTagNames(contentId);

        return Map.of(
                "weather", weather,
                "detail", detail,
                "tags", tags,
                "recommended", recommended
        );
    }

    @GetMapping("/current/by-content")
    public WeatherResponse currentByContent(@RequestParam Long contentId) {
        // 1) 관광지 상세에서 위경도 꺼내오기
        var detail = attractionService.getDetailResponse(contentId);
        double lat = detail.getLat();   // DTO/엔티티의 필드명에 맞게 수정
        double lon = detail.getLon();

        // 2) 해당 좌표로 현재 날씨
        return weatherService.getCurrentWeather(lat, lon);
    }
}
