package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.request.LoginRequest;
import com.moodTrip.spring.domain.member.dto.response.LoginResponse;
import com.moodTrip.spring.domain.member.service.LoginService;
import com.moodTrip.spring.global.common.dto.ApiResponse; // ApiResponse import!
import com.moodTrip.spring.global.common.code.status.SuccessStatus;
import com.moodTrip.spring.global.common.code.status.ErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "로그인", description = "회원 로그인 관련 API")
public class LoginApiController {

    private final LoginService loginService;

    @Operation(summary = "API 로그인", description = "JSON으로 로그인 요청 받아 JWT 토큰 반환")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        String token = loginService.login(loginRequest);
        if (token == null) {
            // 실패 응답: 공통 ApiResponse의 error() 사용
            return ResponseEntity
                    .status(401)
                    .body(ApiResponse.error(ErrorStatus.LOGIN_FAIL));
        }
        // 성공 응답: 공통 ApiResponse의 success() 사용
        return ResponseEntity.ok(
                ApiResponse.success(SuccessStatus.LOGIN_SUCCESS, new LoginResponse(token))
        );
    }
}
