package com.moodTrip.spring.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;  // javax → jakarta로 변경

@Controller
public class MainPageController {

    @GetMapping("/")
    public String mainPage(Model model, HttpSession session) {
        // 로그인 상태 체크
        boolean isLoggedIn = session.getAttribute("memberId") != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "mainpage/mainpage";
    }
}