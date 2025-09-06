package com.moodTrip.spring.domain.member.controller;


import com.moodTrip.spring.domain.member.dto.response.LogoutResponse;
import com.moodTrip.spring.domain.member.dto.response.WithdrawResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberApiControllerTest {

    @Mock
    private MemberService memberService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private MemberApiController memberApiController;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        // 각 테스트 전에 SecurityContext를 초기화하여 테스트 간 영향을 없앱니다.
        SecurityContextHolder.clearContext();
    }

    // --- 로그아웃 테스트 ---

    @Test
    @DisplayName("로그아웃 성공 시 200 OK와 함께 쿠키가 삭제되고 성공 메시지를 반환한다")
    void logout_Success() {
        // given
        Member mockMember = Member.builder().nickname("testUser").build();
        when(securityUtil.getCurrentMember()).thenReturn(mockMember);

        // when
        ResponseEntity<LogoutResponse> responseEntity = memberApiController.logout(request, response);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getMessage()).contains("testUser님, 안전하게 로그아웃되었습니다.");

        // 쿠키가 삭제(maxAge=0)되었는지 검증
        Cookie jwtCookie = response.getCookie("jwtToken");
        Cookie sessionCookie = response.getCookie("JSESSIONID");
        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getMaxAge()).isZero();
        assertThat(sessionCookie).isNotNull();
        assertThat(sessionCookie.getMaxAge()).isZero();
    }

    @Test
    @DisplayName("로그아웃 시 인증되지 않은 사용자라면 401 Unauthorized를 반환한다")
    void logout_whenNotAuthenticated_shouldReturn401() {
        // given
        // securityUtil이 예외를 던지는 상황을 시뮬레이션
        when(securityUtil.getCurrentMember()).thenThrow(new RuntimeException("로그인된 사용자가 없습니다."));

        // when
        ResponseEntity<LogoutResponse> responseEntity = memberApiController.logout(request, response);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getMessage()).contains("인증되지 않은 사용자입니다.");
    }

    // --- 회원 탈퇴 테스트 ---

    @Test
    @DisplayName("회원 탈퇴 성공 시 200 OK와 함께 쿠키가 삭제되고 성공 응답을 반환한다")
    void withdrawMember_Success() {
        // given
        Member mockMember = Member.builder().memberId("testUser").build();
        WithdrawResponse successResponse = WithdrawResponse.builder()
                .success(true).message("탈퇴 처리가 완료되었습니다.").withdrawnAt(LocalDateTime.now()).build();

        when(securityUtil.getCurrentMember()).thenReturn(mockMember);
        when(memberService.withdrawMember(mockMember)).thenReturn(successResponse);

        // when
        ResponseEntity<WithdrawResponse> responseEntity = memberApiController.withdrawMember(request, response);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().isSuccess()).isTrue();
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("탈퇴 처리가 완료되었습니다.");

// Cookie 객체를 직접 가져와 속성을 검증 (더 안정적인 방법)
        Cookie jwtCookie = response.getCookie("jwtToken");
        Cookie jsessionCookie = response.getCookie("JSESSIONID");

        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getValue()).isEmpty(); // 값이 비어있는지 확인
        assertThat(jwtCookie.getMaxAge()).isZero();   // Max-Age가 0인지 확인
        assertThat(jwtCookie.getPath()).isEqualTo("/");
        assertThat(jwtCookie.isHttpOnly()).isTrue();

        assertThat(jsessionCookie).isNotNull();
        assertThat(jsessionCookie.getValue()).isEmpty();
        assertThat(jsessionCookie.getMaxAge()).isZero();
        assertThat(jsessionCookie.getPath()).isEqualTo("/");
    }


    @Test
    @DisplayName("회원 탈퇴 처리 중 RuntimeException 발생 시 400 Bad Request를 반환한다")
    void withdrawMember_whenRuntimeException_shouldReturn400() {
        // given
        // securityUtil은 정상 동작하지만, service 로직에서 예외가 터지는 상황
        Member mockMember = Member.builder().memberId("testUser").build();
        String errorMessage = "테스트용 런타임 예외";
        when(securityUtil.getCurrentMember()).thenReturn(mockMember);
        // 서비스 계층에서 RuntimeException이 발생하는 것을 시뮬레이션
        when(memberService.withdrawMember(mockMember)).thenThrow(new RuntimeException(errorMessage));

        // when
        ResponseEntity<WithdrawResponse> responseEntity = memberApiController.withdrawMember(request, response);

        // then
        // 기대값을 400으로 수정
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().isSuccess()).isFalse();
        // 컨트롤러는 발생한 예외 메시지를 그대로 담아서 반환하므로, 해당 메시지를 검증
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(errorMessage);
    }
}