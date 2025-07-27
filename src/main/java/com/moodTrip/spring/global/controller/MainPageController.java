package com.moodTrip.spring.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 페이지 라우팅을 담당하는 컨트롤러
 * 단순히 HTML 페이지를 반환하는 역할
 */
@Controller
public class MainPageController {

    /**
     * 메인 페이지
     */
    @GetMapping("/")
    public String mainPage() {
        return "mainpage/mainpage";  // templates/mainpage.html 반환
    }

    /**
     * 감정 기반 여행 찾기 페이지
     * 메인페이지의 "감정 기반 여행 찾기" 버튼 클릭 시 이동
     */
    @GetMapping("/emotions")
    public String emotionsPage() {
        return "emotion-search/emotion-search";  // templates/emotion-search/emotion-search.html
    }

    /**
     * 동행매칭 방 만들기 페이지
     * 메인페이지의 "동행매칭 서비스 시작하기" 버튼 클릭 시 이동
     */
    @GetMapping("/creatingRoom/create")
    public String createCompanionRoomPage() {
        return "creatingRoom/creatingRoom-start";  // templates/creatingRoom/creatingRoom-start.html
    }

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login/login";  // templates/login/login.html
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    public String signupPage() {
        return "signup/signup";  // templates/signup/signup.html
    }

    /**
     * 고객센터 페이지
     */
    @GetMapping("/customer-center")
    public String customerCenterPage() {
        return "customer-center/customer-center";  // templates/customer-center/customer-center.html
    }

    /**
     * 마이페이지
     */
    @GetMapping("/my-page")
    public String myPage() {
        return "mypage/mypage";
    }

    /**
     * 공지사항 페이지
     */
    @GetMapping("/notices")
    public String noticesPage() {
        return "customer-center/announcement";
    }

    /**
     * 검색 페이지 (헤더의 검색 기능용)
     */
    @GetMapping("/search")
    public String searchPage() {
        return "search/search-results";
    }
}