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
    //private final WeatherService weatherService;

    @GetMapping("/attractions/detail/{contentId}")
    // 1. 파라미터 타입을 long -> Long 으로 변경
    public String view(@PathVariable("contentId") Long contentId, Model model) {

        // 2. contentId가 null일 경우를 처리하는 방어 코드 추가
        if (contentId == null) {
            // 예를 들어, 목록 페이지나 에러 페이지로 리다이렉트
            // 혹은 적절한 에러 메시지를 담은 뷰를 반환
            return "redirect:/attractions"; // 또는 "error/404" 등
        }

        AttractionDetailResponse detail = attractionService.getDetailResponse(contentId);
        List<String> tagList;
        try {
            tagList = attractionEmotionService.findTagNamesByContentId(contentId);
        } catch (Throwable t) {
            tagList = Collections.emptyList();
        }

        var tags   = attractionService.getEmotionTagNames(contentId); // 감정 태그들



        model.addAttribute("contentId", contentId);
        model.addAttribute("attractionId", detail.getAttractionId());
        model.addAttribute("detail", detail);
        model.addAttribute("tags", tagList);
        model.addAttribute("tags", tags);



        return "recommand-tourist-attractions-detail/detail-page";
    }
}