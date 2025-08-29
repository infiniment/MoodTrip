package com.moodTrip.spring.domain.enteringRoom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestStatsResponse {
    // 통계 데이터 dto

    private Integer totalRequests;      // 총 요청 수
    private Integer todayRequests;      // 오늘 요청 수  
    private Integer urgentRequests;     // 긴급 요청 수 (2시간 이내)
    private Integer pendingRequests;    // 대기 중 요청 수

    // 통계 데이터 생성
    public static RequestStatsResponse of(int total, int today, int urgent, int pending) {
        return RequestStatsResponse.builder()
                .totalRequests(total)
                .todayRequests(today)
                .urgentRequests(urgent)
                .pendingRequests(pending)
                .build();
    }
}