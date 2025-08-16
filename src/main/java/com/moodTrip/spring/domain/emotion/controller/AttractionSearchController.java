package com.moodTrip.spring.domain.emotion.controller;



import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.moodTrip.spring.domain.attraction.service.AttractionServiceImpl;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AttractionSearchController {

    private final AttractionServiceImpl attractionSearchService;

    // JavaScript에서 호출할 API 엔드포인트
    @GetMapping("/api/attractions/search")
    public ResponseEntity<List<AttractionCardDTO>> searchAttractionsByEmotions(
            @RequestParam("emotionIds") List<Integer> emotionIds) {

        List<AttractionCardDTO> attractions = attractionSearchService.findAttractionsByEmotionIds(emotionIds);
        return ResponseEntity.ok(attractions);
    }




}