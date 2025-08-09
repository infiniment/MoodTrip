package com.moodTrip.spring.domain.transport.service;


import com.moodTrip.spring.domain.transport.service.dto.RouteOptionDto;

import java.util.List;

public interface ODsayService {
    /**
     * 대중교통 경로 조회 (경도/위도 순서 유의: X=lng, Y=lat)
     */
    List<RouteOptionDto> getTransitRoutes(double sx, double sy, double ex, double ey);
}