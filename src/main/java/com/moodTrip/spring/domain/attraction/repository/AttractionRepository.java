package com.moodTrip.spring.domain.attraction.repository;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    Optional<Attraction> findByContentId(String contentId); // 중복 방지용
}
