package com.moodTrip.spring.domain.rooms.dto.response;

import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMemberResponse { // 방 참여 응담 DTO
    private Long memberRoomId;
    private Long roomId;
    private Long memberPk;
    private String nickname;
    private String role;
    private Boolean isActive;
    private LocalDateTime joinedAt;

    // DB에서 조회된 엔티티(RoomMember)를 API 응답 객체(DTO)로 변환하는 정적 메서드
    public static RoomMemberResponse from(RoomMember roomMember) {
        return RoomMemberResponse.builder()
                .memberRoomId(roomMember.getMemberRoomId())
                .roomId(roomMember.getRoom().getRoomId())
                .memberPk(roomMember.getMember().getMemberPk())
                .nickname(roomMember.getMember().getNickname())
                .role(roomMember.getRole())
                .isActive(roomMember.getIsActive())
                .joinedAt(roomMember.getJoinedAt())
                .build();
    }



}
