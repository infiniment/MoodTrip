package com.moodTrip.spring.domain.attraction.controller;

import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionRegionResponse;
import com.moodTrip.spring.domain.attraction.dto.request.AttractionInsertRequest;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.global.common.util.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attractions")
@Validated
public class AttractionController {

    private final AttractionService service;
    private final AttractionService attractionService;

    @PostMapping("/add")
    public ResponseEntity<AttractionResponse> createByParams(@ModelAttribute AttractionInsertRequest req) {
        var created = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/attractions/content/" + created.getContentId()))
                .body(created);
    }

//     =============== 목록 동기화  ===============
    @PostMapping("/sync/area")
    public ResponseEntity<SyncAreaResponse> syncArea(
            @RequestParam("areaCode") @Min(1) @Max(99) int areaCode,
            @RequestParam(value = "sigunguCode", required = false) @Min(1) Integer sigunguCode,
            @RequestParam(value = "contentTypeId", required = false) @Min(12) @Max(99) Integer contentTypeId,
            @RequestParam(value = "pageSize", defaultValue = "500") @Min(1) @Max(1000) int pageSize,
            @RequestParam(value = "pauseMillis", defaultValue = "0") @Min(0) long pauseMillis,
            @RequestParam(value = "excludeLodging", defaultValue = "true") boolean excludeLodging,
            @RequestParam(value = "excludeContentTypeIds", required = false) List<@Min(12) @Max(99) Integer> excludeContentTypeIds
    ) {
        Set<Integer> excludes = new HashSet<>();
        if (excludeLodging) excludes.add(12);
        if (excludeContentTypeIds != null) excludes.addAll(excludeContentTypeIds);
        int created = service.syncAreaBasedListExcluding(areaCode, sigunguCode, contentTypeId, pageSize, pauseMillis, excludes);
        return ResponseEntity.accepted().body(
                new SyncAreaResponse("area sync done", areaCode, sigunguCode, contentTypeId, created)
        );
    }

//     =============== 소개 ===============
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

    // =============== 다중 지역 조회 ===============
    @GetMapping("/bulk")
    public ResponseEntity<List<AttractionResponse>> listByAreas(
            @RequestParam("areaCodes") List<@Min(1) @Max(99) Integer> areaCodes,
            @RequestParam(value = "sigunguCode", required = false) @Min(1) Integer sigunguCode,
            @RequestParam(value = "contentTypeId", required = false) @Min(12) @Max(99) Integer contentTypeId
    ) {
        var list = areaCodes.stream()
                .flatMap(area -> service.find(area, sigunguCode, contentTypeId).stream())
                .map(AttractionResponse::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    // =============== 수동 등록 ===============
    @PostMapping
    public ResponseEntity<AttractionResponse> create(@RequestBody @Valid AttractionInsertRequest req) {
        var created = service.create(req);
        return ResponseEntity
                .created(URI.create("/api/attractions/content/" + created.getContentId()))
                .body(created);
    }

    @GetMapping("/search-paged")
    public ResponseEntity<PageResult<AttractionResponse>> searchPaged(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "areaCode", required = false) Integer areaCode,
            @RequestParam(name = "sigunguCode", required = false) Integer sigunguCode,
            @RequestParam(name = "contentTypeId", required = false) Integer contentTypeId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "9") int size
    ) {
        var p = service.searchKeywordPrefTitleStarts(q, areaCode, sigunguCode, contentTypeId, page, size)
                .map(AttractionResponse::from);
        return ResponseEntity.ok(PageResult.of(p));
    }

    // ============== 전 지역 일괄 동기화 (숙박시설 제외)  ===============
    @PostMapping("/sync/area/all")
    public ResponseEntity<SyncAreaAllResponse> syncAreaAll(
            @RequestParam(value = "contentTypeId", required = false) @Min(12) @Max(99) Integer contentTypeId,
            @RequestParam(value = "pageSize", defaultValue = "500") @Min(1) @Max(1000) int pageSize,
            @RequestParam(value = "pauseMillis", defaultValue = "0") @Min(0) long pauseMillis,
            @RequestParam(value = "excludeLodging", defaultValue = "true") boolean excludeLodging,
            @RequestParam(value = "excludeContentTypeIds", required = false) List<@Min(12) @Max(99) Integer> excludeContentTypeIds
    ) {
        Set<Integer> excludes = new HashSet<>();
        if (excludeLodging) excludes.add(12);
        if (excludeContentTypeIds != null) excludes.addAll(excludeContentTypeIds);
        int totalCreated = 0;
        for (int area = 1; area <= 99; area++) {
            totalCreated += service.syncAreaBasedListExcluding(area, null, contentTypeId, pageSize, pauseMillis, excludes);
        }
        return ResponseEntity.accepted().body(
                new SyncAreaAllResponse("area sync all done", contentTypeId, totalCreated)
        );
    }

//     =============== 지정한 여러 지역 동기화 ===============
    @PostMapping("/sync/area/codes")
    public ResponseEntity<SyncAreaCodesResponse> syncAreaCodes(
            @RequestParam("areaCodes") List<@Min(1) @Max(99) Integer> areaCodes,
            @RequestParam(value = "contentTypeId", required = false) @Min(12) @Max(99) Integer contentTypeId,
            @RequestParam(value = "pageSize", defaultValue = "500") @Min(1) @Max(1000) int pageSize,
            @RequestParam(value = "pauseMillis", defaultValue = "0") @Min(0) long pauseMillis,
            @RequestParam(value = "excludeLodging", defaultValue = "true") boolean excludeLodging,
            @RequestParam(value = "excludeContentTypeIds", required = false) List<@Min(12) @Max(99) Integer> excludeContentTypeIds
    ) {
        Set<Integer> excludes = new HashSet<>();
        if (excludeLodging) excludes.add(12);
        if (excludeContentTypeIds != null) excludes.addAll(excludeContentTypeIds);
        int totalCreated = 0;
        for (Integer area : areaCodes) {
            totalCreated += service.syncAreaBasedListExcluding(area, null, contentTypeId, pageSize, pauseMillis, excludes);
        }
        return ResponseEntity.accepted().body(
                new SyncAreaCodesResponse("area sync codes done", areaCodes, contentTypeId, totalCreated)
        );
    }

    // --- 응답 DTO들 (record로 간단하게) ---
    public record SyncAreaResponse(
            String message, int areaCode, Integer sigunguCode, Integer contentTypeId, int created) {}
    public record SyncIntroResponse(
            String message, long contentId, Integer contentTypeId, int saved) {}
    public record SyncIntroByAreaResponse(
            String message, int areaCode, Integer sigunguCode, Integer contentTypeId, int saved) {}


    public record SyncAreaAllResponse(
            String message, Integer contentTypeId, int createdTotal) {}
    public record SyncAreaCodesResponse(
            String message, List<Integer> areaCodes, Integer contentTypeId, int createdTotal) {}


    // AttractionController.java 내
    @GetMapping("/detail-regions")
    public AttractionRegionResponse getByRegions(
            @RequestParam(name = "regions", required = false) List<String> regions,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "numOfRows", required = false) Integer numOfRows,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "cat1", required = false) String cat1,
            @RequestParam(name = "cat2", required = false) String cat2,
            @RequestParam(name = "cat3", required = false) String cat3
    ) {
        int p = (pageNo != null) ? pageNo : (page != null ? page : 1);
        int s = (numOfRows != null) ? numOfRows : (size != null ? size : 12);

        Pageable pageable = PageRequest.of(Math.max(0, p - 1), s, Sort.by("title").ascending());
        return attractionService.findAttractionsFiltered(
                regions, pageable, keyword, cat1, cat2, cat3, sort
        );
    }


    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.unsorted();
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    @GetMapping("/content/{contentId}/detail")
    public ResponseEntity<AttractionDetailResponse> getDetail(@PathVariable long contentId) {
        return ResponseEntity.ok(attractionService.getDetailResponse(contentId));
    }

    @GetMapping("/content/{contentId}/emotion-tags")
    public ResponseEntity<List<String>> getEmotionTags(@PathVariable long contentId) {
        return ResponseEntity.ok(attractionService.getEmotionTagNames(contentId));
    }
}