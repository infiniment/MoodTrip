package com.moodTrip.spring.domain.admin.controller;

import com.moodTrip.spring.domain.admin.dto.response.NotificationResponse;
import com.moodTrip.spring.domain.admin.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final NotificationService notificationService;

    @GetMapping
    public String adminPage(Model model) {
        // 공지사항 목록 가져오기
        model.addAttribute("notices", new ArrayList<>());
//        List<NotificationResponse> notices = notificationService.findAll();
//        model.addAttribute("notices", notices);

        return "admin/admin";
    }
}
