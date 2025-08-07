package com.moodTrip.spring.domain.schedule.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleWebSocketMessage {
    private String type; // "CREATE", "UPDATE", "DELETE"
    private Object data; //  ScheduleResponse or scheduleId (삭제 시)
}
