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
    SCHEDULE_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "SCHEDULE_002", "일정 제목은 필수입니다."),
    SCHEDULE_DATE_INVALID(HttpStatus.BAD_REQUEST, "SCHEDULE_003", "일정 날짜가 유효하지 않습니다."),
    SCHEDULE_DUPLICATED(HttpStatus.CONFLICT, "SCHEDULE_004", "동일한 일정이 이미 존재합니다."),
    SCHEDULE_UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "SCHEDULE_005", "일정에 대한 접근 권한이 없습니다."),

    //기타 도메인 예시,
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "CARD_001", "해당 카드를 찾을 수 없습니다."),
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "ERROR_401", "로그인 실패"), //로그인 실패

    // Room 생성 관련
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_001", "존재하지 않는 방입니다."),
    INVALID_TRAVEL_DATE(HttpStatus.BAD_REQUEST, "ROOM_002", "여행 날짜가 유효하지 않습니다."),
    INVALID_MAX_PARTICIPANT(HttpStatus.BAD_REQUEST, "ROOM_003", "현재 인원보다 적은 최대 인원은 설정할 수 없습니다."),

    // Room 생성 관련
    //EMOTION_NOT_FOUND(HttpStatus.NOT_FOUND, "EMOTION_001", "존재하지 않는 감정 태그입니다."),  // 추가된 부분


    // RoomMember 관련
    ROOM_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "ROOM_MEMBER_001", "이미 해당 방에 참여한 회원입니다."),
    ROOM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_MEMBER_002", "방 참여 정보를 찾을 수 없습니다."),

    // Transport 관련
    TRANSPORT_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "TRANSPORT_001", "대중교통 API 호출 중 오류가 발생했습니다."),
    TRANSPORT_NO_ROUTE(HttpStatus.NOT_FOUND, "TRANSPORT_002", "대중교통 경로를 찾지 못했습니다."),
    TRANSPORT_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "TRANSPORT_003", "대중교통 API 호출 한도를 초과했습니다."),
    TRANSPORT_AUTH_ERROR(HttpStatus.UNAUTHORIZED, "TRANSPORT_004", "대중교통 API 인증에 실패했습니다."),
    TRANSPORT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "TRANSPORT_005", "대중교통 API 요청 파라미터가 유효하지 않습니다.");



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;


}