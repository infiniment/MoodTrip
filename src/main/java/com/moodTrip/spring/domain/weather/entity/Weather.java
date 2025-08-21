package com.moodTrip.spring.domain.weather.entity;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.rooms.entity.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "weather",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_weather_room_dt", columnNames = {"room_id","date_time"})
        },
        indexes = {
                @Index(name = "idx_weather_room_date", columnList = "room_id,date"),
                @Index(name = "idx_weather_attraction_date", columnList = "attraction_id,date") // 조회용 인덱스는 유지 가능
        }
)
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(length = 10, nullable = false) // "yyyy-MM-dd"
    private String date;

    @Column(length = 8, nullable = false) // "HH:mm:ss"
    private String time;

    private double temperature;
    private double feelsLike;
    private int humidity;

    @Column(length = 32)
    private String weather;

    @Column(length = 64)
    private String description;

    @Column(length = 8)
    private String icon;

    private double lat;   // 보존(백필/백업/디버깅용)
    private double lon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id", nullable = false)
    private Attraction attraction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}
