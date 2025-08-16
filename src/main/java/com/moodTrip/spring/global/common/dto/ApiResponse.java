package com.moodTrip.spring.global.common.dto;

import com.moodTrip.spring.global.common.code.status.ErrorStatus;
import com.moodTrip.spring.global.common.code.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    private final boolean isSuccess;  // 성공 여부
    private final String code;        // 응답 코드 (SUCCESS_001, USER_NOT_FOUND 등)
    private final String message;     // 메시지
    private final T data;             // 응답 데이터

    // 성공 응답
    public static <T> ApiResponse<T> success(SuccessStatus successStatus, T data) {
        return ApiResponse.<T>builder()
                .isSuccess(true)
                .code(successStatus.getCode())
                .message(successStatus.getMessage())
                .data(data)
                .build();
    }

    // 에러 응답 (ErrorResponse 대신 통합)
    public static <T> ApiResponse<T> error(ErrorStatus errorStatus) {
        return ApiResponse.<T>builder()
                .isSuccess(false)
                .code(errorStatus.getCode())
                .message(errorStatus.getMessage())
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> errorMessage(String message) {
        return ApiResponse.<T>builder()
                .isSuccess(false)
                .code("ERROR")  // 필요시 커스텀 코드 지정
                .message(message)
                .data(null)
                .build();
    }
}