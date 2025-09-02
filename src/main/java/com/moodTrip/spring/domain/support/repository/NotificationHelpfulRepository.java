package com.moodTrip.spring.domain.support.repository;

import com.moodTrip.spring.domain.admin.entity.Notification;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.support.entity.NotificationHelpful;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationHelpfulRepository extends JpaRepository<NotificationHelpful, Long> {
    Optional<NotificationHelpful> findByNotificationAndMember(Notification notification, Member member);
    boolean existsByNotificationAndMember(Notification notification, Member member);
    long countByNotification(Notification notification);
}