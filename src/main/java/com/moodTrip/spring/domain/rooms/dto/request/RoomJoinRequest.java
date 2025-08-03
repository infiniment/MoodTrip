package com.moodTrip.spring.domain.rooms.dto.request;

public class RoomJoinRequest {
    private Long roomId;
    private Long memberPk;
    private String role; // 예 : MEMBER, LEADER
}
