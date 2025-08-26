package com.moodTrip.spring.domain.attraction.controller;

import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.emotion.service.AttractionEmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AttractionPageController {

    private final AttractionService attractionService;
    private final AttractionEmotionService attractionEmotionService;


    @GetMapping("/attractions/detail/{contentId}")
    public String view(@PathVariable long contentId, Model model) {


        AttractionDetailResponse detail = attractionService.getDetailResponse(contentId);


        List<String> tags;
        try {
            tags = attractionEmotionService.findTagNamesByContentId(contentId);
        } catch (Throwable t) {
            tags = Collections.emptyList();
        }

        // 버튼 프리필용
        model.addAttribute("contentId", contentId);
        model.addAttribute("attractionId", detail.getAttractionId()); // 아래 DTO 수정 반영
        model.addAttribute("detail", detail);
        model.addAttribute("tags", tags);

        // 템플릿 경로 (resources/templates/recommand-tourist-attractions-detail/detail-page.html)
        return "recommand-tourist-attractions-detail/detail-page";
    }
}
