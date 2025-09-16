package com.moodTrip.spring.global.common.exception;

import com.moodTrip.spring.global.common.code.status.ErrorStatus;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorStatus errorStatus;

    public CustomException(ErrorStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
    }
}