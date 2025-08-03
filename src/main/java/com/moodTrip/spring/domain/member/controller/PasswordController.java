package com.moodTrip.spring.domain.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/password")
class PasswordController {

//    private final UserService userService;
//    private final MailService mailService;
//
//    public PasswordController(UserService userService, MailService mailService) {
//        this.userService = userService;
//        this.mailService = mailService;
//    }

    // 비밀번호 찾기 페이지 렌더링 (GET)
    @GetMapping("/api/v1/find")
    public String showPasswordFindForm() {
        return "find-password"; // templates/find-password.html
    }

    // 이메일 전송 요청 (POST)
//    @PostMapping("/send-reset")
//    public String sendResetMail(@RequestParam String email, Model model) {
//        // 1. 유저 존재 여부 확인
//        if (!userService.existsByEmail(email)) {
//            model.addAttribute("error", "가입된 이메일이 없습니다.");
//            return "find-password";
//        }
//        // 2. 임시 비밀번호 생성, 저장, 메일 발송
//        String tempPwd = userService.createTempPasswordAndUpdate(email);
//        mailService.sendTempPassword(email, tempPwd);
//        model.addAttribute("email", email);
//        model.addAttribute("mailSent", true); // 임시 비밀번호 입력창 노출용
//        return "find-password";
//    }

    // 임시 비밀번호를 받아 검증하는 (예시) 로직
//    @PostMapping("/validate-temp")
//    public String validateTempPassword(@RequestParam String email,
//                                       @RequestParam String extra_input,
//                                       Model model) {
//        if (userService.isCorrectTempPassword(email, extra_input)) {
//            model.addAttribute("success", "임시 비밀번호가 일치합니다. 새 비밀번호를 설정하세요.");
//            // 비밀번호 재설정 페이지 등으로 리다이렉트 가능
//        } else {
//            model.addAttribute("error", "임시 비밀번호가 일치하지 않습니다.");
//            model.addAttribute("email", email);
//            model.addAttribute("mailSent", true);
//        }
//        return "find-password";
//    }
}