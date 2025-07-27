package com.moodTrip.spring.domain.schedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScheduleController {

    @GetMapping("/schedule-with-companion/scheduling")
    public String schedulingPage() {
        // templates/schedule-with-companion/scheduling.html 을 반환 test
        return "schedule-with-companion/scheduling";
    }
}