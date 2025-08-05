package com.moodTrip.spring.domain.support.controller;

import com.moodTrip.spring.domain.support.dto.response.NotificationResponse;
import com.moodTrip.spring.domain.support.service.CustomerNotificationService;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer-center")
public class CustomerCenterController {

    private final CustomerNotificationService customerNotificationService;

    // 메인 고객센터 페이지 (공지사항 3개 전달)
    @GetMapping
    public String customerCenter(Model model) {
        List<NotificationResponse> notices = customerNotificationService.findAll().stream()
                .filter(NotificationResponse::getIsVisible)  // 공개된 공지만
                .sorted((a, b) -> b.getRegisteredDate().compareTo(a.getRegisteredDate()))  // 최신순
                .limit(3)  // 상위 3개
                .collect(Collectors.toList());

        model.addAttribute("notices", notices);
        return "customer-center/customer-center";
    }

    // FAQ 목록
    @GetMapping("/faq")
    public String faqPage() {
        return "customer-center/faq";
    }

    // FAQ 상세
    @GetMapping("/faq-detail")
    public String faqDetailPage() {
        return "customer-center/faq-detail";
    }

    // 공지사항 목록
    @GetMapping("/announcement")
    public String announcementPage(Model model,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        // 페이징 처리된 공지사항 목록
        List<NotificationResponse> notices = customerNotificationService.findAll().stream()
                .filter(NotificationResponse::getIsVisible)
                .sorted((a, b) -> b.getRegisteredDate().compareTo(a.getRegisteredDate()))
                .collect(Collectors.toList());

        // 페이징 계산
        int totalItems = notices.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int start = (page - 1) * size;
        int end = Math.min(start + size, totalItems);

        List<NotificationResponse> pageNotices = notices.subList(start, end);

        model.addAttribute("notices", pageNotices);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);

        return "customer-center/announcement";
    }

    // 공지사항 상세
    @GetMapping("/announcement-detail")
    public String announcementDetailPage(@RequestParam Long id, Model model) {
        NotificationResponse notice = customerNotificationService.findById(id);

        // 조회수 증가 (선택사항)
        customerNotificationService.increaseViewCount(id);

        // 이전글/다음글 찾기
        List<NotificationResponse> allNotices = customerNotificationService.findAll().stream()
                .filter(NotificationResponse::getIsVisible)
                .sorted((a, b) -> b.getRegisteredDate().compareTo(a.getRegisteredDate()))
                .collect(Collectors.toList());

        NotificationResponse prevNotice = null;
        NotificationResponse nextNotice = null;

        for (int i = 0; i < allNotices.size(); i++) {
            if (allNotices.get(i).getId().equals(id)) {
                if (i > 0) nextNotice = allNotices.get(i - 1);
                if (i < allNotices.size() - 1) prevNotice = allNotices.get(i + 1);
                break;
            }
        }

        model.addAttribute("notice", notice);
        model.addAttribute("prevNotice", prevNotice);
        model.addAttribute("nextNotice", nextNotice);

        return "customer-center/announcement-detail";
    }
}