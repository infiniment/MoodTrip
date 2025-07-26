package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.request.LoginRequest;
import com.moodTrip.spring.domain.member.dto.response.LoginResponse;
import com.moodTrip.spring.domain.member.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String token = loginService.login(loginRequest);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
