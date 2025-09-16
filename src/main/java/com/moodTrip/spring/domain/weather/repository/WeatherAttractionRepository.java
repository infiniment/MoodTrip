package com.moodTrip.spring.domain.weather.repository;

import com.moodTrip.spring.domain.weather.entity.WeatherAttraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WeatherAttractionRepository extends JpaRepository<WeatherAttraction, Long> {
    Optional<WeatherAttraction> findByAttraction_AttractionIdAndDateTime(Long attractionId, LocalDateTime dateTime);
    boolean existsByAttraction_AttractionIdAndDateTime(Long attractionId, LocalDateTime dateTime);

    // 최신 1건
    Optional<WeatherAttraction> findTopByAttraction_AttractionIdOrderByDateTimeDesc(Long attractionId);

    // 특정 시각 이후 중 최신 1건

    Optional<WeatherAttraction> findTopByAttraction_AttractionIdAndDateTimeGreaterThanEqualOrderByDateTimeDesc(
            Long attractionId, LocalDateTime since);

    // 일자별(yyyy-MM-dd) 시각 순
    List<WeatherAttraction> findByAttraction_AttractionIdAndDateOrderByTimeAsc(Long attractionId, String date);
}