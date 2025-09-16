package com.moodTrip.spring.global.common.exception;

public class WithdrawnMemberException extends RuntimeException {
    public WithdrawnMemberException(String message) {
        super(message);
    }
}
