package com.moodTrip.spring.domain.rooms.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "방 정보 수정 요청 DTO")
public class UpdateRoomRequest {

    @Schema(description = "수정할 방 이름", example = "힐링 여행 모임")
    private String roomName;

    @Schema(description = "수정할 방 설명", example = "함께 힐링 여행을 떠날 사람을 구해요")
    private String roomDescription;

    @Schema(description = "최대 인원 수", example = "4")
    private int maxParticipants;
}
