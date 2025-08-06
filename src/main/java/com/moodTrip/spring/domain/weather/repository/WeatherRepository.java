package com.moodTrip.spring.domain.weather.repository;

import com.moodTrip.spring.domain.weather.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findTopByDateOrderByDateTimeDesc(String date); // 당일 날짜
}
