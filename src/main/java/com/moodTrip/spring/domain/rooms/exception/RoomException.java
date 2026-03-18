package com.moodTrip.spring.domain.rooms.exception;

import com.moodTrip.spring.domain.rooms.enums.RoomErrorCode;
import com.moodTrip.spring.global.common.exception.CustomException;
import lombok.Getter;

@Getter
public class RoomException extends CustomException {
    private final RoomErrorCode errorCode;

    public RoomException(RoomErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public RoomException(RoomErrorCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
    }
}
