package com.moodTrip.spring.domain.attraction.repository;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    Optional<Attraction> findByContentId(Long contentId);

    // 조회 조합별로 DB에서 바로 필터링
    List<Attraction> findAllByAreaCode(int areaCode);
    List<Attraction> findAllByAreaCodeAndSigunguCode(int areaCode, int sigunguCode);
    List<Attraction> findAllByAreaCodeAndContentTypeId(int areaCode, int contentTypeId);
    List<Attraction> findAllByAreaCodeAndSigunguCodeAndContentTypeId(int areaCode, int sigunguCode, int contentTypeId);
}
