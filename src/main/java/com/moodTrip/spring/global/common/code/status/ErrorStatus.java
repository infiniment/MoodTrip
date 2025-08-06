package com.moodTrip.spring.global.common.code.status;

import com.moodTrip.spring.global.common.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 공통 에러
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "접근이 금지되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다."),

    // Auth 관련
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "토큰이 만료되었습니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다."),
    INVALID_USERNAME_OR_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_003", "이메일 또는 비밀번호가 유효하지 않습니다."),

    //User 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),
    DUPLICATE_USER(HttpStatus.CONFLICT, "USER_002", "이미 존재하는 사용자입니다."),

    // Schedule 관련
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_001", "존재하지 않는 일정입니다."),

    //기타 도메인 예시,
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "CARD_001", "해당 카드를 찾을 수 없습니다."),
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "ERROR_401", "로그인 실패"), //로그인 실패

    // Room 생성 관련
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_001", "존재하지 않는 방입니다."),
    INVALID_TRAVEL_DATE(HttpStatus.BAD_REQUEST, "ROOM_002", "여행 날짜가 유효하지 않습니다."),
    INVALID_MAX_PARTICIPANT(HttpStatus.BAD_REQUEST, "ROOM_003", "현재 인원보다 적은 최대 인원은 설정할 수 없습니다."),


    // RoomMember 관련
    ROOM_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "ROOM_MEMBER_001", "이미 해당 방에 참여한 회원입니다."),
    ROOM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_MEMBER_002", "방 참여 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}