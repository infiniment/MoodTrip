package com.moodTrip.spring.domain.weather.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dateTime;   // 예: "2025-08-06 09:00:00"
    private String date;       // 예: "2025-08-06"
    private String time;       // 예: "09:00:00"
    private double temperature;
    private double feelsLike;
    private int humidity;
    private String weather;
    private String description;
    private String icon;
    private double lat;
    private double lon;
}
