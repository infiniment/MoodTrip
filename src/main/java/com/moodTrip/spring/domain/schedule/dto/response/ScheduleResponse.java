package com.moodTrip.spring.domain.schedule.dto.response;

import com.moodTrip.spring.domain.schedule.entity.Schedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScheduleResponse {
    private Long scheduleId;
    private String scheduleTitle;
    private String scheduleDescription;
    private LocalDateTime travelStartDate;
    private LocalDateTime travelEndDate;

    public static ScheduleResponse from(Schedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .scheduleTitle(schedule.getScheduleTitle())
                .scheduleDescription(schedule.getScheduleDescription())
                .travelStartDate(schedule.getTravelStartDate())
                .travelEndDate(schedule.getTravelEndDate())
                .build();
    }

}
