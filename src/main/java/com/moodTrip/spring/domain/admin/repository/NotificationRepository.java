package com.moodTrip.spring.domain.admin.repository;

import com.moodTrip.spring.domain.admin.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTitleContainingOrContentContaining(String title, String content);

}
