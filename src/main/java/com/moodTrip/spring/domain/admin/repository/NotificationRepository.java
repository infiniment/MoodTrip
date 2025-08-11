package com.moodTrip.spring.domain.admin.repository;

import com.moodTrip.spring.domain.admin.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
