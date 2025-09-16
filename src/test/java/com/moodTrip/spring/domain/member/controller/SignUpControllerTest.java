package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.request.MemberRequest;
import com.moodTrip.spring.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private SignUpController signUpController;

    private Model model;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 Model 객체를 새로 생성하여 테스트 간 독립성을 보장합니다.
        model = new BindingAwareModelMap();
    }

    @Test
    @DisplayName("GET /signup 요청 시 회원가입 폼 뷰를 반환한다")
    void signupForm_shouldReturnSignupView() {
        // given
        String error = null; // 에러 파라미터가 없는 경우

        // when
        String viewName = signUpController.signupForm(error, model);

        // then
        assertThat(viewName).isEqualTo("signup/signup");
        // 모델에 빈 MemberRequest 객체가 담겼는지 확인
        assertThat(model.containsAttribute("memberRequest")).isTrue();
    }

    @Test
    @DisplayName("GET /signup?error=... 요청 시 에러 메시지를 모델에 담아 폼 뷰를 반환한다")
    void signupForm_withErrorParam_shouldReturnSignupViewWithError() {
        // given
        String errorMessage = "중복된 아이디입니다.";

        // when
        String viewName = signUpController.signupForm(errorMessage, model);

        // then
        assertThat(viewName).isEqualTo("signup/signup");
        // 모델에 전달된 에러 메시지가 잘 담겼는지 확인
        assertThat(model.getAttribute("errorMessage")).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("POST /signup: 회원가입 성공 시 성공 페이지 뷰를 반환한다")
    void signupSubmit_Success() {
        // given
        MemberRequest memberRequest = new MemberRequest(); // 유효한 회원가입 정보라고 가정
        // memberService.register()가 아무 예외도 던지지 않는 상황을 시뮬레이션
        // (Mockito의 기본 동작)

        // when
        String viewName = signUpController.signupSubmit(memberRequest, model);

        // then
        assertThat(viewName).isEqualTo("signup/success");
        assertThat(model.getAttribute("message")).isEqualTo("회원가입이 성공적으로 완료되었습니다!");
        // memberService의 register 메서드가 해당 memberRequest와 함께 호출되었는지 검증
        verify(memberService).register(memberRequest);
    }

    @Test
    @DisplayName("POST /signup: 회원가입 실패(예외 발생) 시 에러 메시지와 함께 폼 뷰를 다시 반환한다")
    void signupSubmit_Fail_dueToException() {
        // given
        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setUserId("existingUser"); // 실패할 데이터 설정
        String errorMessage = "이미 존재하는 아이디입니다.";

        // memberService.register()가 예외를 던지는 상황을 시뮬레이션
        doThrow(new IllegalStateException(errorMessage)).when(memberService).register(memberRequest);

        // when
        String viewName = signUpController.signupSubmit(memberRequest, model);

        // then
        assertThat(viewName).isEqualTo("signup/signup");
        // 모델에 예외 메시지가 잘 담겼는지 확인
        assertThat(model.getAttribute("errorMessage")).isEqualTo(errorMessage);
        // 사용자가 입력했던 데이터를 유지하기 위해 memberRequest가 모델에 다시 담겼는지 확인
        assertThat(model.getAttribute("memberRequest")).isEqualTo(memberRequest);
    }

    @Test
    @DisplayName("GET /signup/success 요청 시 성공 페이지 뷰를 반환한다")
    void signupSuccess_shouldReturnSuccessView() {
        // when
        String viewName = signUpController.signupSuccess();

        // then
        assertThat(viewName).isEqualTo("signup/success");
    }
}