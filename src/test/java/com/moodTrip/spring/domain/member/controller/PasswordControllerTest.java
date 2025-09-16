package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.request.PasswordForm;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.MailService;
import com.moodTrip.spring.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordControllerTest {

    @Mock
    private MemberService memberService;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordController passwordController;

    private Model model;
    private MockHttpSession session;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        model = new BindingAwareModelMap();
        session = new MockHttpSession();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    // --- 1. 임시 비밀번호 발송 단계 테스트 ---

    @Test
    @DisplayName("임시 비밀번호 발송: 성공")
    void sendTempPassword_Success() {
        // given
        String email = "test@example.com";
        when(memberService.findByEmail(email)).thenReturn(new Member());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // when
        String viewName = passwordController.sendTempPassword(email, model);

        // then
        assertThat(viewName).isEqualTo("find-password/find-password");
        assertThat(model.getAttribute("mailSent")).isEqualTo(true);
        verify(memberService).updatePassword(any(Member.class), anyString());
        verify(mailService).sendTempPasswordMail(eq(email), anyString());
    }

    @Test
    @DisplayName("임시 비밀번호 발송: 실패 (등록되지 않은 이메일)")
    void sendTempPassword_Fail_EmailNotFound() {
        // given
        String email = "notfound@example.com";
        when(memberService.findByEmail(email)).thenReturn(null);

        // when
        String viewName = passwordController.sendTempPassword(email, model);

        // then
        assertThat(viewName).isEqualTo("find-password/find-password");
        assertThat(model.getAttribute("error")).isEqualTo("등록된 이메일이 없습니다.");
        verify(mailService, never()).sendTempPasswordMail(any(), any());
    }

    // --- 2. 임시 비밀번호 검증 단계 테스트 ---

    @Test
    @DisplayName("임시 비밀번호 검증: 성공")
    void validateTempPassword_Success() {
        // given
        String email = "test@example.com";
        String tempPassword = "tempPassword123";
        Member member = Member.builder().email(email).memberPw("encodedPassword").build();

        when(memberService.findByEmail(email)).thenReturn(member);
        when(passwordEncoder.matches(tempPassword, member.getMemberPw())).thenReturn(true);

        // when
        String viewName = passwordController.validateTempPassword(email, tempPassword, model, session);

        // then
        assertThat(viewName).isEqualTo("redirect:/password/new-password");
        assertThat(session.getAttribute("emailVerified")).isEqualTo(email);
    }

    @Test
    @DisplayName("임시 비밀번호 검증: 실패 (비밀번호 불일치)")
    void validateTempPassword_Fail_PasswordMismatch() {
        // given
        String email = "test@example.com";
        String wrongPassword = "wrongPassword";
        Member member = Member.builder().email(email).memberPw("encodedPassword").build();

        when(memberService.findByEmail(email)).thenReturn(member);
        when(passwordEncoder.matches(wrongPassword, member.getMemberPw())).thenReturn(false);

        // when
        String viewName = passwordController.validateTempPassword(email, wrongPassword, model, session);

        // then
        assertThat(viewName).isEqualTo("find-password/find-password");
        assertThat(model.getAttribute("error")).isEqualTo("임시 비밀번호가 일치하지 않습니다.");
        assertThat(session.getAttribute("emailVerified")).isNull();
    }

    // --- 3. 새 비밀번호 재설정 단계 테스트 ---

    @Test
    @DisplayName("비밀번호 재설정: 성공")
    void resetPassword_Success() {
        // given
        String email = "verified@example.com";
        session.setAttribute("emailVerified", email);
        PasswordForm form = new PasswordForm();
        form.setNewPassword("newPassword123");
        form.setConfirmPassword("newPassword123");

        when(memberService.findByEmail(email)).thenReturn(new Member());
        when(passwordEncoder.encode(form.getNewPassword())).thenReturn("encodedNewPassword");

        // when
        String viewName = passwordController.resetPassword(form, session, redirectAttributes, model);

        // then
        assertThat(viewName).isEqualTo("redirect:/login");
        assertThat(redirectAttributes.getFlashAttributes().get("success")).isEqualTo("비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.");
        assertThat(session.getAttribute("emailVerified")).isNull(); // 세션이 삭제되었는지 확인
        verify(memberService).updatePassword(any(Member.class), eq("encodedNewPassword"));
    }

    @Test
    @DisplayName("비밀번호 재설정: 실패 (세션 없음)")
    void resetPassword_Fail_NoSession() {
        // given
        PasswordForm form = new PasswordForm();

        // when
        String viewName = passwordController.resetPassword(form, session, redirectAttributes, model);

        // then
        assertThat(viewName).isEqualTo("redirect:/password/find");
        assertThat(redirectAttributes.getFlashAttributes().get("error")).isEqualTo("인증된 이메일 정보가 없습니다.");
    }

    @Test
    @DisplayName("비밀번호 재설정: 실패 (비밀번호 불일치)")
    void resetPassword_Fail_PasswordMismatch() {
        // given
        String email = "verified@example.com";
        session.setAttribute("emailVerified", email);
        PasswordForm form = new PasswordForm();
        form.setNewPassword("newPassword123");
        form.setConfirmPassword("wrongPassword");

        // when
        String viewName = passwordController.resetPassword(form, session, redirectAttributes, model);

        // then
        assertThat(viewName).isEqualTo("redirect:/password/new-password");
        assertThat(redirectAttributes.getFlashAttributes().get("error")).isEqualTo("비밀번호가 일치하지 않습니다.");
        verify(memberService, never()).updatePassword(any(), any());
    }
}