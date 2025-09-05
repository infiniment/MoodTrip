package com.moodTrip.spring.domain.member.controller;


import com.moodTrip.spring.domain.member.dto.request.LoginRequest;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.LoginService;
import com.moodTrip.spring.domain.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginViewControllerTest {

    @Mock
    private LoginService loginService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private LoginViewController loginViewController;

    private Model model;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        // 테스트에 필요한 Mock 객체들을 초기화합니다.
        model = new BindingAwareModelMap();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("GET /login 요청 시 로그인 폼 뷰를 반환한다")
    void loginPage_shouldReturnLoginView() {
        // when
        String viewName = loginViewController.loginPage(model);

        // then
        assertThat(viewName).isEqualTo("login/login");
        assertThat(model.containsAttribute("loginRequest")).isTrue();
    }

    @Test
    @DisplayName("로그인 실패 시 에러 메시지와 함께 로그인 폼을 다시 보여준다")
    void login_withInvalidCredentials_shouldReturnLoginViewWithError() {
        // given
        LoginRequest loginRequest = new LoginRequest("invalidUser", "wrongPassword");
        when(loginService.login(any(LoginRequest.class))).thenReturn(null); // 로그인 실패 시 null 반환

        // when
        String viewName = loginViewController.login(loginRequest, response, model, request);

        // then
        assertThat(viewName).isEqualTo("login/login");
        assertThat(model.getAttribute("error")).isEqualTo("아이디 또는 비밀번호가 일치하지 않습니다.");
        assertThat(response.getCookie("jwtToken")).isNull(); // 쿠키가 발급되지 않았는지 확인
    }

    @Test
    @DisplayName("탈퇴한 회원이 로그인 시도 시 전용 페이지로 이동시킨다")
    void login_withWithdrawnMember_shouldReturnWithdrawView() {
        // given
        LoginRequest loginRequest = new LoginRequest("withdrawnUser", "password");
        Member withdrawnMember = Member.builder().memberId("withdrawnUser").isWithdraw(true).build();

        when(loginService.login(any(LoginRequest.class))).thenReturn("fake-token");
        when(memberService.findByMemberId("withdrawnUser")).thenReturn(withdrawnMember);

        // when
        String viewName = loginViewController.login(loginRequest, response, model, request);

        // then
        assertThat(viewName).isEqualTo("login/withdraw");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("이미 탈퇴하신 회원입니다.");
        assertThat(response.getCookie("jwtToken")).isNull(); // 쿠키가 발급되지 않았는지 확인
    }

    @Test
    @DisplayName("일반 회원 로그인 성공 시 쿠키를 발급하고 메인 페이지로 리다이렉트한다")
    void login_withValidUser_shouldRedirectToMain() {
        // given
        LoginRequest loginRequest = new LoginRequest("user", "password");
        Member normalUser = Member.builder().memberId("user").memberPk(2L).isWithdraw(false).build();

        when(loginService.login(any(LoginRequest.class))).thenReturn("user-token");
        when(memberService.findByMemberId("user")).thenReturn(normalUser);

        // when
        String viewName = loginViewController.login(loginRequest, response, model, request);

        // then
        assertThat(viewName).isEqualTo("redirect:/");
        assertThat(request.getSession().getAttribute("isAdmin")).isEqualTo(false); // 세션 확인

        Cookie jwtCookie = response.getCookie("jwtToken");
        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getValue()).isEqualTo("user-token"); // 쿠키 값 확인
    }

    @Test
    @DisplayName("관리자 로그인 성공 시 isAdmin 세션을 true로 설정하고 리다이렉트한다")
    void login_withAdminUser_shouldSetAdminSessionAndRedirect() {
        // given
        LoginRequest loginRequest = new LoginRequest("admin", "adminPassword");
        Member adminUser = Member.builder().memberId("admin").memberPk(1L).isWithdraw(false).build();

        when(loginService.login(any(LoginRequest.class))).thenReturn("admin-token");
        when(memberService.findByMemberId("admin")).thenReturn(adminUser);

        // when
        String viewName = loginViewController.login(loginRequest, response, model, request);

        // then
        assertThat(viewName).isEqualTo("redirect:/");
        assertThat(request.getSession().getAttribute("isAdmin")).isEqualTo(true); // 관리자 세션 확인

        Cookie jwtCookie = response.getCookie("jwtToken");
        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getValue()).isEqualTo("admin-token");
    }
}
