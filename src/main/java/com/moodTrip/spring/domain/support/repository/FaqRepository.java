package com.moodTrip.spring.domain.support.repository;

import com.moodTrip.spring.domain.support.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {
}

