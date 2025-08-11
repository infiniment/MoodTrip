package com.moodTrip.spring.domain.weather.controller;

import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import com.moodTrip.spring.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;
    private final RoomRepository roomRepository; // ★ 임시로 주입

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
}
