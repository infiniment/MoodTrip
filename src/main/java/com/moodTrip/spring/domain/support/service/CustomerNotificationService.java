package com.moodTrip.spring.domain.support.service;

import com.moodTrip.spring.domain.admin.entity.Notification;
import com.moodTrip.spring.domain.admin.repository.NotificationRepository;
import com.moodTrip.spring.domain.support.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerNotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> findAll() {
        return notificationRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public NotificationResponse findById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        return convertToResponse(notification);
    }

    // 조회수 증가 메서드 추가
    @Transactional  // 읽기 전용이 아니므로 @Transactional 필요
    public void increaseViewCount(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        // 조회수 증가
        notification.setViewCount(notification.getViewCount() + 1);
        notificationRepository.save(notification);
    }

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getNoticeId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setIsVisible(notification.getIsVisible());
        response.setIsImportant(notification.getIsImportant());
        response.setRegisteredDate(notification.getRegisteredDate().atStartOfDay());
        response.setViewCount(notification.getViewCount());
        return response;
    }
}