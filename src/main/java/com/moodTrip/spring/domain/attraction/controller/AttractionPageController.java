package com.moodTrip.spring.domain.attraction.controller;

import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.emotion.service.AttractionEmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
@Controller
@RequiredArgsConstructor
public class AttractionPageController {

    private final AttractionService attractionService;
    private final AttractionEmotionService attractionEmotionService;
    //private final WeatherService weatherService;

    @GetMapping("/attractions/detail/{contentId}")
    public String view(
            @PathVariable("contentId") Long contentId,
            @RequestParam(value = "contentTypeId", required = false) Integer contentTypeId,
            Model model
    ) {
        if (contentId == null) return "redirect:/attractions";

        AttractionDetailResponse detail;
        try {
            // ✅ 오버로드 사용 (서비스가 내부에서 contentTypeId 보강도 함)
            detail = attractionService.getDetailResponse(contentId, contentTypeId);
        } catch (IllegalArgumentException notFound) {
            // DB에 Attraction이 없을 때 404 처리
            model.addAttribute("message", notFound.getMessage());
            return "error/404";
        }

        List<String> tags;
        try {
            tags = attractionEmotionService.findTagNamesByContentId(contentId);
        } catch (Throwable t) {
            tags = Collections.emptyList();
        }

        model.addAttribute("contentId", contentId);
        model.addAttribute("attractionId", detail.getAttractionId());
        model.addAttribute("detail", detail);
        model.addAttribute("tags", tags);

        return "recommand-tourist-attractions-detail/detail-page";
    }


}