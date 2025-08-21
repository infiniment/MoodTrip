package com.moodTrip.spring.domain.mypageRoom.dto.response;

import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 내가 입장한 방 목록 조회 => 참여중인 방에 표시
@Schema(description = "내가 입장한 방 정보 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinedRoomResponse {

    private Long roomId;
    private String roomName;
    private String roomDescription;
    private Integer maxCount;
    private Integer currentCount;
    private LocalDate travelStartDate;
    private LocalDate travelEndDate;
    private String destinationCategory;
    private String destinationName;
    private String creatorNickname;
    private String myRole;
    private LocalDateTime joinedAt;
    private LocalDateTime roomCreatedAt;
    private List<String> emotions;
    private String image;

     // 데이터베이스에서 가져온 엔티티 데이터를 프론트엔드가 사용할 수 있는 형태로 변환

    public static JoinedRoomResponse from(RoomMember roomMember) {
        Room room = roomMember.getRoom();  // 참여한 방 정보 가져오기

        List<String> emotionTexts = room.getEmotionRooms().stream()
                .map(er -> er.getEmotion().getTagName())
                .toList();

        String image = (room.getAttraction() != null && room.getAttraction().getFirstImage() != null)
                ? room.getAttraction().getFirstImage()
                : "/static/image/default.png";

        return JoinedRoomResponse.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .roomDescription(room.getRoomDescription())
                .maxCount(room.getRoomMaxCount())
                .currentCount(room.getRoomCurrentCount())
                .travelStartDate(room.getTravelStartDate())
                .travelEndDate(room.getTravelEndDate())
                .destinationCategory(room.getDestinationCategory())
                .destinationName(room.getDestinationName())
                .creatorNickname(room.getCreator().getNickname())  // 방장 닉네임
                .myRole(roomMember.getRole())  // 내 역할
                .joinedAt(roomMember.getJoinedAt())  // 내가 방에 참여한 시간
                .roomCreatedAt(room.getCreatedAt())  // 방 생성 시간
                .emotions(emotionTexts)
                .image(image)
                .build();
    }
}