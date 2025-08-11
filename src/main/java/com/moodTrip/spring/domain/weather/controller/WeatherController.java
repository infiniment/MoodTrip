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
