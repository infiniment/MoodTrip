package com.moodTrip.spring.domain.mypageRoom.dto.response;

import com.moodTrip.spring.domain.rooms.entity.Room;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatedRoomResponse {
    private Long roomId;
    private String roomName;
    private String roomDescription;
    private Integer maxCount;
    private Integer currentCount;
    private LocalDate travelStartDate;
    private LocalDate travelEndDate;
    private String destinationCategory;
    private String destinationName;
    private LocalDateTime createdAt;
    private List<String> emotions;
    private String image;

    public static CreatedRoomResponse from(Room room) {
        List<String> emotionTexts = room.getEmotionRooms().stream()
                .map(er -> er.getEmotion().getTagName())
                .toList();

        String image = (room.getAttraction() != null && room.getAttraction().getFirstImage() != null)
                ? room.getAttraction().getFirstImage()
                : "/image/fix/moodtrip.png";

        return CreatedRoomResponse.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .roomDescription(room.getRoomDescription())
                .maxCount(room.getRoomMaxCount())
                .currentCount(room.getRoomCurrentCount())
                .travelStartDate(room.getTravelStartDate())
                .travelEndDate(room.getTravelEndDate())
                .destinationCategory(room.getDestinationCategory())
                .destinationName(room.getDestinationName())
                .createdAt(room.getCreatedAt())
                .emotions(emotionTexts)
                .image(image)
                .build();
    }
}
