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
        MemberFireResponse response = memberFireService.reportMember(roomId, request);
        return ResponseEntity.ok(response); // 항상 200 OK
    }

}
