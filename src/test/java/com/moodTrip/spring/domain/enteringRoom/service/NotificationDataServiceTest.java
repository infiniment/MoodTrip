package com.moodTrip.spring.domain.enteringRoom.service;

import com.moodTrip.spring.domain.enteringRoom.dto.response.NotificationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDataServiceTest {

    private NotificationDataService notificationDataService;

    @BeforeEach
    void setUp() {
        notificationDataService = new NotificationDataService();
    }

    @Test
    @DisplayName("알림 저장 및 존재 여부 확인")
    void saveNotification_and_hasNotification() {
        // given
        Long memberPk = 1L;
        NotificationData notification = NotificationData.builder()
                .type("ROOM_APPROVED")
                .roomName("테스트 방")
                .message("승인되었습니다!")
                .timestamp(LocalDateTime.now())
                .build();

        // when
        notificationDataService.saveNotification(memberPk, notification);

        // then
        assertThat(notificationDataService.hasNotification(memberPk)).isTrue();
    }

    @Test
    @DisplayName("알림 조회 후 삭제 동작 확인")
    void getAndClearNotification() {
        // given
        Long memberPk = 2L;
        NotificationData notification = NotificationData.builder()
                .type("ROOM_REJECTED")
                .roomName("거절된 방")
                .message("거절되었습니다.")
                .timestamp(LocalDateTime.now())
                .build();

        notificationDataService.saveNotification(memberPk, notification);

        // when
        NotificationData fetched = notificationDataService.getAndClearNotification(memberPk);

        // then
        assertThat(fetched).isNotNull();
        assertThat(fetched.getType()).isEqualTo("ROOM_REJECTED");
        assertThat(notificationDataService.hasNotification(memberPk)).isFalse(); // ✅ 조회 후 삭제됨
    }

    @Test
    @DisplayName("존재하지 않는 알림 조회 시 null 반환")
    void getAndClearNotification_whenEmpty() {
        // given
        Long memberPk = 3L;

        // when
        NotificationData result = notificationDataService.getAndClearNotification(memberPk);

        // then
        assertThat(result).isNull();
    }
}
