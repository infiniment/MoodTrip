package com.moodTrip.spring.global.common.exception;

import com.moodTrip.spring.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    // 📌 기존 CustomException 처리
    @ExceptionHandler(CustomException.class)
    public Object handleCustomException(CustomException e, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(e.getErrorStatus().getHttpStatus())
                    .body(ApiResponse.error(e.getErrorStatus()));
        } else {
            request.setAttribute("errorMessage", e.getErrorStatus().getMessage());
            return "error/custom-error";
        }
    }

    // 📌 탈퇴 회원 처리
    @ExceptionHandler(WithdrawnMemberException.class)
    public Object handleWithdrawnMember(WithdrawnMemberException e, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.errorMessage("탈퇴하신 회원입니다."));
        } else {
            request.setAttribute("errorMessage", "탈퇴하신 회원입니다.");
            return "error/custom-error";
        }
    }

    // 📌 API 요청 여부 판별
    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }
}
