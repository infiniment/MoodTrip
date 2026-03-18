package com.moodTrip.spring.global.common.code.status;

import com.moodTrip.spring.global.common.code.ResponseCode;

public enum SuccessStatus implements ResponseCode {

    LOGIN_SUCCESS("AUTH_002", "로그인에 성공했습니다.");

    private final String statusCode;
    private final String message;

    SuccessStatus(String statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public String getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getCode() {
        return statusCode;
    }
}
