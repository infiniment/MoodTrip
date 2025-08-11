package com.moodTrip.spring.domain.weather.repository;

import com.moodTrip.spring.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findTopByRoom_RoomIdOrderByDateTimeAsc(Long roomId);
    List<Weather> findByRoom_RoomIdAndDateOrderByTimeAsc(Long roomId, String date);
    List<Weather> findByRoom_RoomIdAndDateBetweenOrderByDateAscTimeAsc(Long roomId, String start, String end);
}