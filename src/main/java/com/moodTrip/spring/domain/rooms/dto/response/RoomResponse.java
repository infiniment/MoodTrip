package com.moodTrip.spring.domain.rooms.dto.response;

import com.moodTrip.spring.domain.rooms.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {

    private Long roomId;
    private String roomName;
    private String roomDescription;
    private int maxParticipants;
    private int currentParticipants;

    private String travelStartDate;
    private String travelEndDate;

    private Boolean isDeleted;

    // 정적 팩토리 메서드
    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getRoomId(),
                room.getRoomName(),
                room.getRoomDescription(),
                room.getRoomMaxCount(),
                room.getRoomCurrentCount(),
                room.getTravelStartDate() != null ? room.getTravelStartDate().toString() : null,
                room.getTravelEndDate() != null ? room.getTravelEndDate().toString() : null,
                room.getIsDeleteRoom()
        );
    }
}
