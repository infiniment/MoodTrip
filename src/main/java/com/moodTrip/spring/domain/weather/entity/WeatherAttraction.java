package com.moodTrip.spring.domain.weather.entity;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@Table(
        name = "weather_attraction",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_weather_attraction_dt",
                        columnNames = {"attraction_id", "date_time"})
        },
        indexes = {
                @Index(name = "idx_weather_attraction_date",
                        columnList = "attraction_id,date")
        }
)
public class WeatherAttraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // yyyy-MM-dd HH:mm:ss 로 저장할 실제 시각
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;
    private String formattedDateTime;

    @Column(length = 10, nullable = false) // yyyy-MM-dd
    private String date;

    @Column(length = 8, nullable = false)  // HH:mm:ss
    private String time;

    private Long contentId;
    private double temperature;
    private double feelsLike;
    private int humidity;

    @Column(length = 32)
    private String weather;

    @Column(length = 64)
    private String description;

    @Column(length = 8)
    private String icon;

    // 백업/디버깅용 좌표 (서울 고정이라도 보존)
    private double lat;
    private double lon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attraction_id", nullable = false)
    private Attraction attraction;
}