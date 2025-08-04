package com.moodTrip.spring.domain.support.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerCenterController {
    @GetMapping("/customer-center")
    public String customerCenterPage() {
        return "customer-center/customer-center";
    }
    @GetMapping("/customer-center/faq")
    public String faqPage() {
        return "customer-center/faq";
    }

    @GetMapping("/customer-center/faq-detail")
    public String faqDetailPage() {
        return "customer-center/faq-detail";
    }

    @GetMapping("/customer-center/announcement")
    public String announcementPage() {
        return "customer-center/announcement";
    }

    @GetMapping("/customer-center/announcement-detail")
    public String announcementDetailPage() {
        return "customer-center/announcement-detail";
    }
}
