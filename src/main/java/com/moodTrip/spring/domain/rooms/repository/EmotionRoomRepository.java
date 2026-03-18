package com.moodTrip.spring.domain.rooms.repository;

import com.moodTrip.spring.domain.rooms.entity.EmotionRoom;
import com.moodTrip.spring.domain.rooms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface EmotionRoomRepository extends JpaRepository<EmotionRoom, Long> {
    @EntityGraph(attributePaths = {"emotion"})
    List<EmotionRoom> findByRoomIn(List<Room> rooms);
}
