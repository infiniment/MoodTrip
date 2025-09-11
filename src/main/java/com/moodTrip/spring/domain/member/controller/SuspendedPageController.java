package com.moodTrip.spring.domain.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SuspendedPageController {

    @GetMapping("/suspended")
    public String suspendedPage() {
        return "login/suspended";
    }
}
