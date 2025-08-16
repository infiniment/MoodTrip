package com.moodTrip.spring.domain.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WithdrawPageController {

    @GetMapping("/withdraw")
    public String withdrawPage() {
        return "login/withdraw";
    }
}
