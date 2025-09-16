package com.moodTrip.spring.domain.transport.controller;

import com.moodTrip.spring.domain.transport.dto.response.KakaoAddressSearchResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoCoord2AddressResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoPlaceResponse;
import com.moodTrip.spring.domain.transport.service.ODsayService;
import com.moodTrip.spring.domain.transport.service.TransportService;
import com.moodTrip.spring.domain.transport.service.dto.RouteOptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transport")
@RequiredArgsConstructor
public class TransportApiController {

    private final TransportService service;
    private final ODsayService odsayService;

    @GetMapping("/kakao/search")
    public KakaoPlaceResponse search(
            @RequestParam("query") String query,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y,
            @RequestParam(value = "radius", required = false) Integer radius) {
        return service.searchPlace(query, x, y, radius);
    }

    @GetMapping("/kakao/geocode")
    public KakaoAddressSearchResponse geocode(@RequestParam("address") String address) {
        return service.geocode(address);
    }

    @GetMapping("/kakao/reverse-geocode")
    public KakaoCoord2AddressResponse reverse(@RequestParam("x") double x, @RequestParam("y") double y) {
        return service.reverseGeocode(x, y);
    }

    @GetMapping("/routes")
    public ResponseEntity<List<RouteOptionDto>> routes (
            @RequestParam("sx") double sx, @RequestParam("sy") double sy,
            @RequestParam("ex") double ex, @RequestParam("ey") double ey) {
        return ResponseEntity.ok(odsayService.getTransitRoutes(sx, sy, ex, ey));
    }
}
