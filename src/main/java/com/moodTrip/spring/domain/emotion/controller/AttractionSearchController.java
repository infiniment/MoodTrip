package com.moodTrip.spring.domain.emotion.controller;



import com.moodTrip.spring.domain.emotion.dto.response.AttractionCardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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


    // ✅ 새 페이징 응답
    @GetMapping("/api/attractions/search/paged")
    public ResponseEntity<Page<AttractionCardDTO>> searchAttractionsByEmotionsPaged(
            @RequestParam("emotionIds") List<Integer> emotionIds,
            @PageableDefault(page = 0, size = 20, sort = "attraction_id", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<AttractionCardDTO> page =
                attractionSearchService.findAttractionsByEmotionIdsPaged(emotionIds, pageable);
        return ResponseEntity.ok(page);
    }

}