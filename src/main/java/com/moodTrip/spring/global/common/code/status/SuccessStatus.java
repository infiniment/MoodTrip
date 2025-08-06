package com.moodTrip.spring.global.common.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessStatus {
    OK("SUCCESS_200", "요청이 성공적으로 처리되었습니다."),
    CREATED("SUCCESS_201", "리소스가 성공적으로 생성되었습니다."),
    LOGIN_SUCCESS("SUCCESS_200" , "로그인 성공");

    private final String code;
    private final String message;
}