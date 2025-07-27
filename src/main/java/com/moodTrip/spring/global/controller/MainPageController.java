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
     * http://localhost:8080/ 접속 시 mainpage.html 반환
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
     * 동행자 찾기 페이지 (누락된 경로 추가)
     * 메인페이지의 "동행자 찾기" 버튼 클릭 시 이동
     */
    @GetMapping("/companion-rooms")
    public String companionRoomsPage() {
        // 임시로 메인페이지로 리다이렉트 (실제 페이지가 준비되기 전까지)
        return "redirect:/";
        // return "companion-rooms/room-list";  // 실제 페이지가 있을 때 사용
    }

    /**
     * 동행매칭 방 만들기 페이지
     * 메인페이지의 "동행매칭 서비스 시작하기" 버튼 클릭 시 이동
     */
    @GetMapping("/companion-rooms/create")
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
        return "mypage/mypage";  // templates/mypage/mypage.html
    }

    /**
     * 공지사항 페이지
     */
    @GetMapping("/notices")
    public String noticesPage() {
        return "notices/notices";  // templates/notices/notices.html
    }

    /**
     * 검색 페이지 (헤더의 검색 기능용)
     */
    @GetMapping("/search")
    public String searchPage() {
        return "search/search-results";  // templates/search/search-results.html
    }
}