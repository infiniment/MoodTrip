package com.moodTrip.spring.domain.attraction.controller;

import com.moodTrip.spring.domain.attraction.service.AttractionService; // ✅ 서비스 주입
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AttractionPageController {

    private final AttractionService attractionService;
    @GetMapping("/templates/recommand-tourist-attractions-detail/detail-page.html")
    public String view(@RequestParam(required = false) Long contentId, Model model) {
        if (contentId != null) {
            attractionService.getDetail(contentId)
                    .ifPresent(a -> model.addAttribute("attraction", a));
            model.addAttribute("contentId", contentId);
        }
        return "recommand-tourist-attractions-detail/detail-page";
    }
}
