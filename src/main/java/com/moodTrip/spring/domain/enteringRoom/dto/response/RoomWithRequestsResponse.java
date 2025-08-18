package com.moodTrip.spring.domain.enteringRoom.dto.response;

import com.moodTrip.spring.domain.rooms.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomWithRequestsResponse {

    // 방과 신청 통합 정보 dto
    private Long roomId;
    private String roomTitle;
    private String travelDate;           // "25/05/13" 형식
    private Integer currentParticipants;
    private Integer maxParticipants;
    private Integer pendingRequestsCount; // 대기 중인 신청 수
    private List<JoinRequestListResponse> joinRequests; // 신청 목록

    // 🔥 Room 엔티티에서 DTO로 변환
    public static RoomWithRequestsResponse from(Room room, List<JoinRequestListResponse> requests) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd");
        String formattedDate = room.getTravelStartDate() != null
                ? room.getTravelStartDate().format(formatter)
                : "미정";

        return RoomWithRequestsResponse.builder()
                .roomId(room.getRoomId())
                .roomTitle(room.getRoomName())
                .travelDate(formattedDate)
                .currentParticipants(room.getRoomCurrentCount())
                .maxParticipants(room.getRoomMaxCount())
                .pendingRequestsCount(requests.size())
                .joinRequests(requests)
                .build();
    }
}