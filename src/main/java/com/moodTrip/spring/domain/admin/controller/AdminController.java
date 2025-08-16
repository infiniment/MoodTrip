package com.moodTrip.spring.domain.admin.controller;

import com.moodTrip.spring.domain.admin.service.FaqService;
import com.moodTrip.spring.domain.admin.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final NotificationService notificationService;
    private final FaqService faqService;

    @GetMapping
    public String adminPage(Model model) {
        // 공지사항 목록 가져오기
        model.addAttribute("notices", new ArrayList<>());

        // FAQ 목록 가져오기
        model.addAttribute("faqs", faqService.findAll());

        return "admin/admin";
    }







}
