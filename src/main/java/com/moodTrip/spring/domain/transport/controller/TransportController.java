package com.moodTrip.spring.domain.transport.controller;

import com.moodTrip.spring.domain.transport.service.ODsayService;
import com.moodTrip.spring.domain.transport.service.dto.RouteOptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transport")
public class TransportController {

    private final ODsayService odsayService;


    // 예 : /api/transport/routes?sx=126.978438&sy=37.566668&ex=127.02758&ey=37.49794
    @GetMapping("/routes")
    public ResponseEntity<List<RouteOptionDto>> routes (
            @RequestParam("sx") double sx, @RequestParam("sy") double sy,
            @RequestParam("ex") double ex, @RequestParam("ey") double ey) {
        return ResponseEntity.ok(odsayService.getTransitRoutes(sx, sy, ex, ey));
    }


}
