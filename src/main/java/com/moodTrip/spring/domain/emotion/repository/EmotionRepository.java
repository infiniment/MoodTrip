package com.moodTrip.spring.domain.emotion.repository;

import com.moodTrip.spring.domain.rooms.entity.EmotionRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmotionRepository extends JpaRepository<EmotionRoom, Long> {

}
