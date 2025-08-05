package com.moodTrip.spring.domain.rooms.dto.response;

import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.global.common.exception.CustomException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

import static com.moodTrip.spring.domain.member.entity.QMember.member;
import static com.moodTrip.spring.global.common.code.status.ErrorStatus.INTERNAL_SERVER_ERROR;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMemberResponse {

    @Schema(description = "방 참여자 ID", example = "1")
    private Long memberRoomId;

    @Schema(description = "방 ID", example = "1")
    private Long roomId;

    @Schema(description = "사용자 PK", example = "3")
    private Long memberPk;

    @Schema(description = "사용자 닉네임", example = "김상우")
    private String nickname;

    @Schema(description = "역할", example = "MEMBER")
    private String role;

    @Schema(description = "방 참여 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "방 참여 시각", example = "2025-08-04T06:41:15.711Z")
    private LocalDateTime joinedAt;

    public static RoomMemberResponse from(RoomMember roomMember) {

        if (member == null) {
            throw new CustomException(INTERNAL_SERVER_ERROR);
        }

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