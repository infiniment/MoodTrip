package com.moodTrip.spring.domain.attraction.repository;

import com.moodTrip.spring.domain.attraction.entity.UserAttraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<UserAttraction, Long> {

    void deleteByMember_MemberPkAndAttraction_AttractionId(Long memberPk, Long attractionId);
}