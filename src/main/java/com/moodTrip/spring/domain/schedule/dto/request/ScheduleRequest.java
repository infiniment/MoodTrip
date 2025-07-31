package com.moodTrip.spring.domain.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRequest {
    @Schema(description = "일정 날짜 (yyyy-MM-dd)", example = "2025-07-26")
    private LocalDate date;

    @Schema(description = "일정 시간 (HH:mm)", example = "11:00")
    private LocalTime time;

    @Schema(description = "일정 제목", example = "서울역 집합")
    private String title;

    @Schema(description = "일정 설명 (선택)", example = "KTX 타고 수원으로 이동")
    private String description;
}
