package com.moodTrip.spring.domain.attraction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AttractionViewController {

    // 렌더링 시작
    @GetMapping("/regions")
    public String showRegionPage() {
        return "region-tourist-attractions/region-page";
    }
}
