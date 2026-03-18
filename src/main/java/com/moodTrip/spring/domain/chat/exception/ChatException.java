package com.moodTrip.spring.domain.chat.exception;

import com.moodTrip.spring.domain.chat.exception.code.ChatErrorCode;
import com.moodTrip.spring.global.common.exception.CustomException;
import lombok.Getter;


@Getter
public class ChatException extends CustomException {
    private final ChatErrorCode errorCode;

    public ChatException(ChatErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public ChatException(ChatErrorCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }

    public ChatException(ChatErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.errorCode = errorCode;
    }

    public static ChatException of(ChatErrorCode errorCode) {
        return new ChatException(errorCode);
    }

    public static ChatException of(ChatErrorCode errorCode, String message) {
        return new ChatException(errorCode, message);
    }

    public static ChatException invalidChatRoomId() {
        return new ChatException(ChatErrorCode.INVALID_CHAT_ROOM_ID);
    }

    public static ChatException invalidSender() {
        return new ChatException(ChatErrorCode.INVALID_SENDER);
    }

    public static ChatException emptyMessage() {
        return new ChatException(ChatErrorCode.EMPTY_MESSAGE);
    }

    public static ChatException invalidMessageType() {
        return new ChatException(ChatErrorCode.INVALID_MESSAGE_TYPE);
    }

    public static ChatException redisPublishFailed(Throwable cause) {
        return new ChatException(
                ChatErrorCode.REDIS_PUBLISH_FAILED,
                ChatErrorCode.REDIS_PUBLISH_FAILED.getMessage(),
                cause
        );
    }

    public static ChatException serializationFailed(Throwable cause) {
        return new ChatException(
                ChatErrorCode.CHAT_SERIALIZATION_FAILED,
                ChatErrorCode.CHAT_SERIALIZATION_FAILED.getMessage(),
                cause
        );
    }
}
