package com.moodTrip.spring.global.common.code.status;

import com.moodTrip.spring.global.common.code.ResponseCode;

public enum ErrorStatus implements ResponseCode {

    ROOM_NOT_FOUND("ROOM_001", "존재하지 않는 방입니다."),
    SCHEDULE_NOT_FOUND("SCHEDULE_001", "존재하지 않는 일정입니다."),

    TRANSPORT_AUTH_ERROR("TRANSPORT_001", "대중교통 API 인증에 실패했습니다."),
    TRANSPORT_RATE_LIMIT("TRANSPORT_002", "대중교통 API 요금 제한에 걸렸습니다."),
    TRANSPORT_BAD_REQUEST("TRANSPORT_003", "대중교통 API 요청이 올바르지 않습니다."),
    TRANSPORT_API_ERROR("TRANSPORT_004", "대중교통 API 호출 중 오류가 발생했습니다."),
    TRANSPORT_NO_ROUTE("TRANSPORT_005", "도착지까지 경로를 찾을 수 없습니다."),

    WEBSOCKET_FORBIDDEN("WEBSOCKET_001", "WebSocket 연결 권한이 없습니다."),

    FORBIDDEN("COMMON_002", "요청 권한이 없습니다."),
    INTERNAL_SERVER_ERROR("COMMON_001", "서버 내부 오류가 발생했습니다."),

    LOGIN_FAIL("AUTH_001", "로그인에 실패했습니다.");

    private final String statusCode;
    private final String message;

    ErrorStatus(String statusCode, String message) {
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
