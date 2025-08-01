package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.request.LoginRequest;
import com.moodTrip.spring.domain.member.service.LoginService;
import com.moodTrip.spring.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@Tag(name = "로그인", description = "회원 로그인 관련 화면")
public class LoginViewController {

    private final LoginService loginService;
    private final MemberService memberService;


    @Operation(summary = "로그인 폼", description = "로그인 화면을 반환")
    @GetMapping("/api/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login/login";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login/login"; // 실제 로그인 폼 템플릿 경로 (Thymeleaf 등)
    }

    @Operation(summary = "로그인 처리", description = "회원 로그인 요청(폼 전송 방식)을 처리한다")
    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest, HttpServletResponse response , Model model) {
        log.info("로그인 요청: id={}, pw={}", loginRequest.getMemberId(), loginRequest.getMemberPw());

        String token = loginService.login(loginRequest);

        if (token == null) {
            // 로그인 실패
            model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
            model.addAttribute("loginRequest", loginRequest);
            return "login/login"; // 로그인 폼 재출력
        }

        // 쿠키 생성 (HttpOnly, Secure 옵션은 필요에 따라 설정)
        Cookie jwtCookie = new Cookie("jwtToken", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60); // 1일 (단위: 초)
        response.addCookie(jwtCookie);


        return "redirect:/"; // 성공 시 메인 페이지 등으로 이동
    }

    //소셜 로그인 성공 시
    @GetMapping("/mainpage/mainpage")
    public String mainPage() {
        return "mainpage/mainpage"; // templates/mainpage/mainpage.html 필요
    }


}
