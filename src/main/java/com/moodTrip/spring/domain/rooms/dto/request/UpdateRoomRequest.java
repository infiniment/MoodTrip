package com.moodTrip.spring.domain.rooms.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoomRequest {
    private String roomName;
    private String roomDescription;
    private int maxParticipants;
}
