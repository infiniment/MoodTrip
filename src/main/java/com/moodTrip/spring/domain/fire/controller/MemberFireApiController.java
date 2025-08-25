package com.moodTrip.spring.domain.fire.controller;

import com.moodTrip.spring.domain.fire.dto.request.MemberFireRequest;
import com.moodTrip.spring.domain.fire.dto.response.MemberFireResponse;
import com.moodTrip.spring.domain.fire.service.MemberFireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/fires")
@RequiredArgsConstructor
public class MemberFireApiController {

    private final MemberFireService memberFireService;

    @PostMapping("/rooms/{roomId}/members")
    public ResponseEntity<MemberFireResponse> reportMember(
            @PathVariable Long roomId,
            @RequestBody MemberFireRequest request
    ) {
        try {
            MemberFireResponse response = memberFireService.reportMember(roomId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("❌ 신고 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(MemberFireResponse.failure(e.getMessage()));
        }
    }
}
