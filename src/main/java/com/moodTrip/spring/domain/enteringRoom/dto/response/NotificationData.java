package com.moodTrip.spring.domain.enteringRoom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 입장 신청 승인/거절 모달 나오기 위한 dto
public class NotificationData {
    private String type;        // "ROOM_APPROVED" 또는 "ROOM_REJECTED"
    private String roomName;    // 방 이름
    private String message;     // 알림 메시지
    private LocalDateTime timestamp;
}