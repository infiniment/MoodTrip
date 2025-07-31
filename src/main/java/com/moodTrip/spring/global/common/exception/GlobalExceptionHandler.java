package com.moodTrip.spring.global.common.exception;

import com.moodTrip.spring.global.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = {
        com.moodTrip.spring.domain.member.controller.LoginApiController.class,
})

public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        return ResponseEntity
                .status(e.getErrorStatus().getHttpStatus())
                .body(ApiResponse.error(e.getErrorStatus()));
    }
}
