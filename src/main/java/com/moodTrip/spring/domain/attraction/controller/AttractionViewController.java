package com.moodTrip.spring.domain.attraction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AttractionViewController {

    // 렌더링 시작
    @GetMapping("/regions")
    public String regionPage(Model model) {
        model.addAttribute("initialAttractions", List.of());
        return "region-tourist-attractions/region-page";
    }
}
