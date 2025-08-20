package com.moodTrip.spring.domain.weather.dto.response;

import com.moodTrip.spring.domain.weather.entity.Weather;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponse {

    private String dateTime;
    private String date;
    private String time;
    private double temperature;
    private double feelsLike;
    private int humidity;
    private String weather;
    private String description;
    private String icon;
    private double lat;
    private double lon;

    private Double maxTemp;
    private Double minTemp;

    public WeatherResponse(Weather weather) {
        this.dateTime = weather.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.date = weather.getDate();
        this.time = weather.getTime();
        this.temperature = weather.getTemperature();
        this.feelsLike = weather.getFeelsLike();
        this.humidity = weather.getHumidity();
        this.weather = weather.getWeather();
        this.description = weather.getDescription();
        this.icon = weather.getIcon();
        this.lat = weather.getLat();
        this.lon = weather.getLon();
    }
}
