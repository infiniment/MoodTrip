package com.moodTrip.spring.domain.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRequest {
    @Schema(description = "일정 제목", example = "서울역 집합")
    private String scheduleTitle;

    @Schema(description = "일정 설명 (선택)", example = "KTX 타고 수원으로 이동")
    private String scheduleDescription;

    @Schema(description = "일정 시작 시간 (yyyy-MM-ddTHH:mm)", example = "2025-08-08T11:00")
    private LocalDateTime travelStartDate;
}