package com.moodTrip.spring.domain.support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moodTrip.spring.domain.admin.entity.Faq;


public interface FaqRepository extends JpaRepository<Faq, Long> {
}

