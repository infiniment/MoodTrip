package com.moodTrip.spring.domain.weather.entity;

import com.moodTrip.spring.domain.rooms.entity.Room;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "weather",
        uniqueConstraints = {
                // 같은 관광지 + 같은 시각 데이터 중복 방지
                @UniqueConstraint(name = "uk_weather_attraction_dt", columnNames = {"attraction_id","date_time"})
        },
        indexes = {
                @Index(name = "idx_weather_attraction_date", columnList = "attraction_id,date"),
                @Index(name = "idx_weather_attraction_datetime", columnList = "attraction_id,date_time")
        }
)
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_time", length = 19, nullable = false) // "yyyy-MM-dd HH:mm:ss"
    private String dateTime;

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

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "attraction_id", nullable = false)
//    private Attraction attraction;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false) // 임시 키
    private Room room;
}
