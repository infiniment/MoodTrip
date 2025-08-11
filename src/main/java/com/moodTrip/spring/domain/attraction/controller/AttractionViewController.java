package com.moodTrip.spring.domain.attraction.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class AttractionViewController {

    // 렌더링 시작
    @GetMapping("/regions")
    public String showRegionPage() {
        // This will tell Spring to render the 'region-page.html' template
        return "region-page";
    }
}
