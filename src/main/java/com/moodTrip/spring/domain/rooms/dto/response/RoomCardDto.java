package com.moodTrip.spring.domain.rooms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomCardDto {
    private Long roomId;
    private String roomName;
    private String roomDescription;
    private String destinationCategory;
    private String destinationName;
    private int maxParticipants;         // roomMaxCount
    private int currentParticipants;     // roomCurrentCount
    private String travelStartDate;      // yyyy-MM-dd (toString or format)
    private String travelEndDate;        // yyyy-MM-dd
    private String image;                // 대표 이미지 (ex. null or 기본 이미지)
    private List<String> tags;           // 감정 태그 등 (EmotionRoom 등 join 시 활용)
    private String status;               // 모집중/마감임박 등
    private String createDate;           // 생성일자 (BaseEntity.getCreatedAt() 활용)
}
