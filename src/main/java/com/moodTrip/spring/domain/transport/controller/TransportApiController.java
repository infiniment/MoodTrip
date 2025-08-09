package com.moodTrip.spring.domain.transport.controller;

import com.moodTrip.spring.domain.transport.dto.response.KakaoAddressSearchResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoCoord2AddressResponse;
import com.moodTrip.spring.domain.transport.dto.response.KakaoPlaceResponse;
import com.moodTrip.spring.domain.transport.service.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transport")
@RequiredArgsConstructor
public class TransportApiController {

    private final TransportService service;

    @GetMapping("/kakao/search")
    public KakaoPlaceResponse search(
            @RequestParam String query,
            @RequestParam(required = false) Double x,
            @RequestParam(required = false) Double y,
            @RequestParam(required = false) Integer radius) {
        return service.searchPlace(query, x, y, radius);
    }

    @GetMapping("/kakao/geocode")
    public KakaoAddressSearchResponse geocode(@RequestParam String address) {
        return service.geocode(address);
    }

    @GetMapping("/kakao/reverse-geocode")
    public KakaoCoord2AddressResponse reverse(@RequestParam double x, @RequestParam double y) {
        return service.reverseGeocode(x, y);
    }
}
