package com.moodTrip.spring.domain.emotion.controller;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.service.AttractionService; // 인터페이스로 받기
import com.moodTrip.spring.domain.emotion.dto.request.EmotionWeightDto;
import com.moodTrip.spring.domain.emotion.service.AttractionEmotionService;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/attraction-emotions")
@RequiredArgsConstructor
public class AttractionEmotionController {

    private final AttractionEmotionService attractionEmotionService;
    private final AttractionService attractionService;
    private final EmotionService emotionService;

    @GetMapping
    public String showMappingPage(@RequestParam(name="page",defaultValue = "0") int page,
                                  @RequestParam(name="size",defaultValue = "10") int size,
                                  Model model) {
        Page<Attraction> attractionPage = attractionService.findAttractions(page, size);

        int currentPage = attractionPage.getNumber();
        int totalPages  = attractionPage.getTotalPages();
        int window      = 10; // 페이지 버튼 10개만 보이도록

        int startPage = Math.max(0, currentPage - window / 2);
        int endPage   = Math.min(totalPages - 1, startPage + window - 1);
        if (endPage - startPage < window - 1) {
            startPage = Math.max(0, endPage - window + 1);
        }

        // ✅ 관광지 목록 (페이징된 10개)
        model.addAttribute("attractions", attractionPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        // ✅ 감정 카테고리 목록
        model.addAttribute("emotions", emotionService.getAllEmotions());

        // ✅ 관광지별 감정 매핑 정보 (체크박스/가중치 초기화용)
        Map<Long, List<Long>> attractionToEmotionIds = attractionEmotionService.getAttractionToEmotionIdsMap();
        model.addAttribute("attractionToEmotionIds", attractionToEmotionIds);

        Map<Long, Map<Long, BigDecimal>> attractionToEmotionWeights =
                attractionEmotionService.getAttractionToEmotionWeightsMap();
        model.addAttribute("attractionToEmotionWeights", attractionToEmotionWeights);

        return "admin/attraction-emotion-mapping :: content";
    }

    // 검색 목록
    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(name="keyword") String keyword,
                         Model model) {
        Page<Attraction> p = attractionService.searchAttractions(keyword.trim(), page, size);
        return buildModelAndView(p, page, size, keyword, model);
    }

    private String buildModelAndView(Page<Attraction> attractionPage,
                                     int page, int size, String keyword, Model model) {
        int currentPage = attractionPage.getNumber();
        int totalPages  = attractionPage.getTotalPages();
        int window = 10;

        int startPage = Math.max(0, currentPage - window / 2);
        int endPage   = Math.min(totalPages - 1, startPage + window - 1);
        if (endPage - startPage < window - 1) {
            startPage = Math.max(0, endPage - window + 1);
        }

        model.addAttribute("attractions", attractionPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        // 🔑 검색어 유지(검색 화면일 때만 값이 존재)
        model.addAttribute("keyword", keyword == null ? "" : keyword);

        // 감정/가중치 초기화용
        model.addAttribute("emotions", emotionService.getAllEmotions());
        model.addAttribute("attractionToEmotionIds", attractionEmotionService.getAttractionToEmotionIdsMap());
        model.addAttribute("attractionToEmotionWeights", attractionEmotionService.getAttractionToEmotionWeightsMap());

        return "admin/attraction-emotion-mapping :: content"; // ":: content" 필수
    }
    @PostMapping("/update/{attractionId}")
    @ResponseBody
    public ResponseEntity<?> updateAttractionEmotion(
            @PathVariable Long attractionId,
            @RequestBody List<EmotionWeightDto> emotionWeights
    ) {
        try {
            attractionEmotionService.updateAttractionEmotions(attractionId, emotionWeights);
            return ResponseEntity.ok(Map.of("message", "Attraction emotions updated successfully")); // ✅ JSON 안전
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));              // ✅ JSON 안전
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal server error"));                               // ✅ 상세 메시지는 로그로
        }
    }
}
