package com.moodTrip.spring.domain.rooms.enums;

import com.moodTrip.spring.global.common.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomErrorCode implements ResponseCode {

    ROOM_NOT_FOUND("R400_1", "존재하지 않는 방입니다."),
    USER_NOT_FOUND("R400_2", "존재하지 않는 사용자입니다."),
    INVALID_DATE("R400_3", "유효하지 않은 여행 일자입니다."),
    ATTRACTION_NOT_FOUND("R400_4", "어트랙션을 찾을 수 없습니다."),
    INVALID_MAX_PARTICIPANT("R400_5", "최대 인원 수가 유효하지 않습니다."),
    ROOM_MEMBER_ALREADY_EXISTS("R400_6", "이미 방에 참여 중인 사용자입니다."),
    ROOM_MEMBER_NOT_FOUND("R400_7", "해당 방 참여자를 찾을 수 없습니다."),
    INVALID_EMOTION("R400_8", "유효하지 않은 감정 태그입니다."),
    ROOM_FORBIDDEN("R403_1", "해당 방에 대한 권한이 없습니다.");

    private final String statusCode;
    private final String message;
}
