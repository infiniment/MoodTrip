package com.moodTrip.spring.domain.fire.dto.response;

import com.moodTrip.spring.domain.fire.entity.RoomFire;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomFireResponse {

    private boolean success;
    private String message;
    private Long fireId;
    private Long roomId;
    private String roomTitle;
    private LocalDateTime firedAt;
    private String fireReason;

    public static RoomFireResponse success(RoomFire fire) {
        return RoomFireResponse.builder()
                .success(true)
                .message("신고가 정상적으로 접수되었습니다. 검토 후 적절한 조치를 취하겠습니다.")
                .fireId(fire.getFireId())
                .roomId(fire.getFiredRoom().getRoomId())
                .roomTitle(fire.getFiredRoom().getRoomName())
                .firedAt(fire.getCreatedAt())
                .fireReason(fire.getFireReason().getDescription())
                .build();
    }

    public static RoomFireResponse failure(String message, Long roomId, String roomTitle) {
        return RoomFireResponse.builder()
                .success(false)
                .message(message)
                .fireId(null)
                .roomId(roomId)
                .roomTitle(roomTitle)
                .firedAt(null)
                .fireReason(null)
                .build();
    }

    public static RoomFireResponse failure(String message) {
        return RoomFireResponse.builder()
                .success(false)
                .message(message)
                .fireId(null)
                .roomId(null)
                .roomTitle(null)
                .firedAt(null)
                .fireReason(null)
                .build();
    }
}