package com.moodTrip.spring.domain.transport.service.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RouteOptionDto {
    private String routeSummary;  // "지하철+버스 1회 환승"
    private int totalTime;      // 분
    private int fare;           // 원
    private int transferCount;
    private List<String> segments; // ["지하철 2호선 12분", "버스 9401 25분", "도보 5분"]
    private String externalUrl; // 카카오맵 자세히보기 링크
}