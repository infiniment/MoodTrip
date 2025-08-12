package com.moodTrip.spring.domain.attraction.controller;

import com.moodTrip.spring.domain.attraction.dto.request.AttractionInsertRequest;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attractions")
@Validated
public class AttractionController {

    private final AttractionService service;

    // =============== 목록 동기화 (areaBasedList2) ===============
    @PostMapping("/sync/area")
    public ResponseEntity<SyncAreaResponse> syncArea(
            @RequestParam("areaCode") @Min(1) @Max(99) int areaCode,
            @RequestParam(value = "sigunguCode", required = false) @Min(1) Integer sigunguCode,
            @RequestParam(value = "contentTypeId", required = false) @Min(12) @Max(99) Integer contentTypeId,
            @RequestParam(value = "pageSize", defaultValue = "500") @Min(1) @Max(1000) int pageSize,
            @RequestParam(value = "pauseMillis", defaultValue = "0") @Min(0) long pauseMillis
    ) {
        int created = service.syncAreaBasedList(areaCode, sigunguCode, contentTypeId, pageSize, pauseMillis);
        return ResponseEntity.accepted().body(
                new SyncAreaResponse("area sync done", areaCode, sigunguCode, contentTypeId, created)
        );
    }

    // =============== 소개(detailIntro2) ===============
    @PostMapping("/sync/intro")
    public ResponseEntity<SyncIntroResponse> syncIntro(
            @RequestParam("contentId") @Positive long contentId,
            @RequestParam(value = "contentTypeId", required = false) @Min(12) @Max(99) Integer contentTypeId
    ) {
        int saved = service.syncDetailIntro(contentId, contentTypeId);
        return ResponseEntity.accepted().body(
                new SyncIntroResponse("intro sync ok", contentId, contentTypeId, saved)
        );
    }

    @PostMapping("/sync/intro/by-area")
    public ResponseEntity<SyncIntroByAreaResponse> syncIntroByArea(
            @RequestParam("areaCode") @Min(1) @Max(99) int areaCode,
            @RequestParam(value = "sigunguCode", required = false) @Min(1) Integer sigunguCode,
            @RequestParam(value = "contentTypeId", required = false) @Min(12) @Max(99) Integer contentTypeId,
            @RequestParam(value = "pauseMillis", defaultValue = "0") @Min(0) long pauseMillis
    ) {
        int saved = service.syncDetailIntroByArea(areaCode, sigunguCode, contentTypeId, pauseMillis);
        return ResponseEntity.accepted().body(
                new SyncIntroByAreaResponse("intro sync by area ok", areaCode, sigunguCode, contentTypeId, saved)
        );
    }

    // =============== 조회 API ===============
    @GetMapping
    public ResponseEntity<List<AttractionResponse>> list(
            @RequestParam("areaCode") @Min(1) @Max(99) int areaCode,
            @RequestParam(value = "sigunguCode", required = false) @Min(1) Integer sigunguCode,
            @RequestParam(value = "contentTypeId", required = false) @Min(12) @Max(99) Integer contentTypeId
    ) {
        return ResponseEntity.ok(
                service.find(areaCode, sigunguCode, contentTypeId)
                        .stream().map(AttractionResponse::from).collect(toList())
        );
    }

    // =============== 수동 등록 ===============
    @PostMapping
    public ResponseEntity<AttractionResponse> create(@RequestBody @Valid AttractionInsertRequest req) {
        var created = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/attractions/content/" + created.getContentId()))
                .body(created);
    }

    @GetMapping("/search")
    public ResponseEntity<List<AttractionResponse>> search(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "areaCode", required = false) @Min(1) @Max(99) Integer areaCode,
            @RequestParam(name = "sigunguCode", required = false) @Min(1) Integer sigunguCode,
            @RequestParam(name = "contentTypeId", required = false) @Min(12) @Max(99) Integer contentTypeId,
            @RequestParam(name = "limit", defaultValue = "30") @Min(1) @Max(100) Integer limit
    ) {
        // 필터가 오면 DB 필터 조회
        if (areaCode != null || sigunguCode != null || contentTypeId != null) {
            int ac = (areaCode != null) ? areaCode : 1;
            return ResponseEntity.ok(
                    service.find(ac, sigunguCode, contentTypeId)   // 엔티티 반환
                            .stream()
                            .map(AttractionResponse::from)          // 엔티티 → DTO
                            .collect(java.util.stream.Collectors.toList())
            );
        }

        // 키워드 없으면 추천 TOP N (이미 DTO)
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(service.getRecommendedTop(limit));
        }

        // 키워드 검색 (이미 DTO)
        return ResponseEntity.ok(service.searchByKeyword(q, limit));
    }

    // --- 응답 DTO들 (record로 간단하게) ---
    public record SyncAreaResponse(
            String message, int areaCode, Integer sigunguCode, Integer contentTypeId, int created) {}
    public record SyncIntroResponse(
            String message, long contentId, Integer contentTypeId, int saved) {}
    public record SyncIntroByAreaResponse(
            String message, int areaCode, Integer sigunguCode, Integer contentTypeId, int saved) {}
}