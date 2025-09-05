package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.request.LoginRequest;
import com.moodTrip.spring.domain.member.dto.response.LoginResponse;
import com.moodTrip.spring.domain.member.service.LoginService;
import com.moodTrip.spring.global.common.code.status.ErrorStatus;
import com.moodTrip.spring.global.common.code.status.SuccessStatus;
import com.moodTrip.spring.global.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginApiControllerTest {

    @Mock
    private LoginService loginService;

    @InjectMocks
    private LoginApiController loginApiController;

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 공통 요청 객체 생성
        loginRequest = new LoginRequest("test@example.com", "password123");
    }

    @Test
    @DisplayName("로그인 성공 시 JWT 토큰과 200 OK를 반환한다")
    void login_Success() {
        // given
        String fakeToken = "fake-jwt-token";
        when(loginService.login(any(LoginRequest.class))).thenReturn(fakeToken);

        // when
        ResponseEntity<ApiResponse<LoginResponse>> response = loginApiController.login(loginRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getCode()).isEqualTo(SuccessStatus.LOGIN_SUCCESS.getCode());
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getToken()).isEqualTo(fakeToken);
    }

    @Test
    @DisplayName("로그인 실패 시 null 토큰과 401 Unauthorized를 반환한다")
    void login_Fail() {
        // given
        when(loginService.login(any(LoginRequest.class))).thenReturn(null);

        // when
        ResponseEntity<ApiResponse<LoginResponse>> response = loginApiController.login(loginRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getCode()).isEqualTo(ErrorStatus.LOGIN_FAIL.getCode());
        assertThat(response.getBody().getData()).isNull();
    }
}
