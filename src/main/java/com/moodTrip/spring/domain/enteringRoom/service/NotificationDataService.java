package com.moodTrip.spring.domain.enteringRoom.service;

import com.moodTrip.spring.domain.enteringRoom.dto.response.NotificationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Slf4j
@Service
public class NotificationDataService {

    // 메모리에 임시 저장 (실제 운영에서는 Redis 사용 권장)
    private final Map<Long, NotificationData> notifications = new ConcurrentHashMap<>();

    /**
     * 알림 저장
     */
    public void saveNotification(Long memberPk, NotificationData notification) {
        notifications.put(memberPk, notification);
        log.info("알림 저장 완료 - 회원PK: {}, 타입: {}", memberPk, notification.getType());
    }

    /**
     * 알림 조회 후 삭제 (한 번만 표시)
     */
    public NotificationData getAndClearNotification(Long memberPk) {
        NotificationData notification = notifications.remove(memberPk);
        if (notification != null) {
            log.info("알림 조회 및 삭제 - 회원PK: {}, 타입: {}", memberPk, notification.getType());
        }
        return notification;
    }

    /**
     * 알림 존재 여부 확인
     */
    public boolean hasNotification(Long memberPk) {
        return notifications.containsKey(memberPk);
    }
}