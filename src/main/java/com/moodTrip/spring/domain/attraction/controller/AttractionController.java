package com.moodTrip.spring.domain.attraction.controller;

import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attractions") // Weather: /api/weather 와 동일 레벨
public class AttractionController {

    private final AttractionService attractionService;
    private final AttractionRepository attractionRepository;

    @GetMapping
    public ResponseEntity<List<Attraction>> getAll() {
        return ResponseEntity.ok(attractionRepository.findAll());
    }

    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync(
            @RequestParam("areaCode") int areaCode,
            @RequestParam(name = "contentTypeId", defaultValue = "12") int contentTypeId
    ) {
        int affected = attractionService.fetchAndSaveAttractions(areaCode, contentTypeId);
        return ResponseEntity.ok(Map.of(
                "message", "저장(업서트) 완료",
                "affected", affected,
                "areaCode", areaCode,
                "contentTypeId", contentTypeId
        ));
    }
}
