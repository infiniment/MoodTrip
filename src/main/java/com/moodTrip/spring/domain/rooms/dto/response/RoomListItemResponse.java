package com.moodTrip.spring.domain.rooms.dto.response;

import com.moodTrip.spring.domain.rooms.entity.Room;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "방 목록 아이템 응답 DTO")
public class RoomListItemResponse {

    @Schema(description = "방 ID", example = "1")
    private Long roomId;

    @Schema(description = "방 이름", example = "서울 근교 힐링 방")
    private String roomName;

    @Schema(description = "방 설명", example = "함께 힐링 여행 떠날 동행자를 구합니다")
    private String roomDescription;

    @Schema(description = "관광지 ID", example = "101")
    private Long attractionId;

    @Schema(description = "여행 목적지 카테고리", example = "강원도")
    private String destinationCategory;

    @Schema(description = "여행 목적지 이름", example = "속초 해수욕장")
    private String destinationName;

    @Schema(description = "여행 목적지 위도", example = "37.5665357")
    private BigDecimal destinationLat;

    @Schema(description = "여행 목적지 경도", example = "126.9779692")
    private BigDecimal destinationLon;

    @Schema(description = "최대 인원 수", example = "4")
    private int maxParticipants;

    @Schema(description = "현재 인원 수", example = "2")
    private int currentParticipants;

    @Schema(description = "여행 시작일 (yyyy-MM-dd)", example = "2025-08-01")
    private String travelStartDate;

    @Schema(description = "여행 종료일 (yyyy-MM-dd)", example = "2025-08-03")
    private String travelEndDate;

    @Schema(description = "대표 이미지", example = "/image/creatingRoom/landscape-placeholder-svgrepo-com.svg")
    private String imageUrl;

    public static RoomListItemResponse from(Room room) {
        RoomListItemResponseBuilder builder = RoomListItemResponse.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .roomDescription(room.getRoomDescription())
                .maxParticipants(room.getRoomMaxCount())
                .currentParticipants(room.getRoomCurrentCount())
                .travelStartDate(room.getTravelStartDate() != null ? room.getTravelStartDate().toString() : null)
                .travelEndDate(room.getTravelEndDate() != null ? room.getTravelEndDate().toString() : null);

        if (room.getAttraction() != null) {
            builder.attractionId(room.getAttraction().getAttractionId());

            String image = room.getAttraction().getFirstImage();
            builder.imageUrl(image != null ? image : "/image/creatingRoom/landscape-placeholder-svgrepo-com.svg");

            String title = room.getAttraction().getTitle();
            builder.destinationName(title != null && !title.isEmpty() ? title : room.getDestinationName());

            Double lat = room.getAttraction().getMapY();
            Double lon = room.getAttraction().getMapX();
            builder.destinationLat(lat != null ? BigDecimal.valueOf(lat) : room.getDestinationLat());
            builder.destinationLon(lon != null ? BigDecimal.valueOf(lon) : room.getDestinationLon());
            builder.destinationCategory(room.getDestinationCategory());
        } else {
            builder.destinationCategory(room.getDestinationCategory())
                    .destinationName(room.getDestinationName())
                    .destinationLat(room.getDestinationLat())
                    .destinationLon(room.getDestinationLon())
                    .imageUrl("/image/creatingRoom/landscape-placeholder-svgrepo-com.svg");
        }

        return builder.build();
    }
}
