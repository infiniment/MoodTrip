package com.moodTrip.spring.domain.weather.repository;

import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.weather.entity.Weather;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Set;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    Optional<Weather> findTopByRoom_RoomIdOrderByDateTimeAsc(Long roomId);
    List<Weather> findByRoom_RoomIdAndDateOrderByTimeAsc(Long roomId, String date);
    List<Weather> findByRoom_RoomIdAndDateBetweenOrderByDateAscTimeAsc(Long roomId, String start, String end);


    Optional<Weather> findTopByAttraction_AttractionIdOrderByDateTimeDesc(Long attractionId);

    Optional<Weather> findTopByAttraction_AttractionIdAndDateTimeGreaterThanEqualOrderByDateTimeDesc(
            Long attractionId, LocalDateTime since
    );

    @Query("select w.dateTime from Weather w where w.room.roomId = :roomId")
    Set<LocalDateTime> findAllDateTimesByRoomId(@Param("roomId") Long roomId);
}