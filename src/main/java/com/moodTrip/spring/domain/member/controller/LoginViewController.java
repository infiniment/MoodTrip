package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.request.LoginRequest;
import com.moodTrip.spring.domain.member.service.LoginService;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@Tag(name = "로그인", description = "회원 로그인 관련 화면")
public class LoginViewController {

    private final LoginService loginService;
    private final MemberService memberService;
    private final RoomService roomService;

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
    public String login(@ModelAttribute LoginRequest loginRequest,
                        HttpServletResponse response, Model model, HttpServletRequest request) {

        log.info("로그인 요청: id={}, pw={}", loginRequest.getMemberId(), loginRequest.getMemberPw());

        // 로그인 시도
        String token = loginService.login(loginRequest);

        if (token == null) {
            // 로그인 실패
            model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
            model.addAttribute("loginRequest", loginRequest);
            return "login/login"; // 로그인 폼 재출력
        }

        // 🔹 로그인 성공했으니 회원 정보 조회
        var member = memberService.findByMemberId(loginRequest.getMemberId());

        // 🔹 탈퇴 회원이면 withdraw.html로 이동
        if (member.getIsWithdraw() != null && member.getIsWithdraw()) {
            log.info("🚫 탈퇴한 회원 로그인 시도: {}", loginRequest.getMemberId());
            model.addAttribute("errorMessage", "이미 탈퇴하신 회원입니다.");
            return "login/withdraw"; // templates/login/withdraw.html
        }

        // 🔹 memberPk가 1이면 관리자용 스타일을 추가
        if (member.getMemberPk() == 1) {
            request.getSession().setAttribute("isAdmin", true); // 세션에 관리자 플래그 추가
            log.info("관리자 로그인: isAdmin = true, memberPk = {}", member.getMemberPk()); // 관리자일 때 로그 출력
        } else {
            request.getSession().setAttribute("isAdmin", false); // 일반 사용자로 설정
            log.info("일반 사용자 로그인: isAdmin = false, memberPk = {}", member.getMemberPk()); // 일반 사용자일 때 로그 출력
        }


        // 정상 회원 → JWT 쿠키 발급
        Cookie jwtCookie = new Cookie("jwtToken", token);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        //jwtCookie.setMaxAge(7200); // 2시간
        response.addCookie(jwtCookie);

        // 메인 페이지로 이동dd
        return "redirect:/";
    }


    //소셜 로그인 성공 시dd
//    @GetMapping("/mainpage/mainpage")
//    public String mainPage(Model model) {
//        log.info("==== [RoomController] /mainpage/mainpage 진입 ====");
//        List<RoomResponse> rooms = roomService.getAllRooms();
//        log.info("rooms 개수: {}", rooms.size());
//        model.addAttribute("rooms", rooms);
//        return "mainpage/mainpage"; // 뷰 파일명
//    }

}
