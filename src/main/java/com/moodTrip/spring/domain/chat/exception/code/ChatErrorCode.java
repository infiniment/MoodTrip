package com.moodTrip.spring.domain.chat.exception.code;

import com.moodTrip.spring.global.common.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatErrorCode implements ResponseCode {

    INVALID_CHAT_ROOM_ID("CHAT_400_1", "유효하지 않은 채팅방 ID입니다."),
    CHAT_ROOM_NOT_FOUND("CHAT_404_1", "존재하지 않는 채팅방입니다."),
    INVALID_SENDER("CHAT_400_2", "유효하지 않은 발신자입니다."),
    EMPTY_MESSAGE("CHAT_400_3", "메시지 내용이 비어 있습니다."),
    MESSAGE_TOO_LONG("CHAT_400_4", "메시지 길이가 제한을 초과했습니다."),
    INVALID_MESSAGE_TYPE("CHAT_400_5", "지원하지 않는 메시지 타입입니다."),
    REDIS_PUBLISH_FAILED("CHAT_500_1", "채팅 메시지 발행 중 오류가 발생했습니다."),
    REDIS_SUBSCRIBE_FAILED("CHAT_500_2", "채팅 메시지 수신 중 오류가 발생했습니다."),
    CHAT_SERIALIZATION_FAILED("CHAT_500_3", "채팅 메시지 직렬화/역직렬화에 실패했습니다.");

    private final String statusCode;
    private final String message;
}
