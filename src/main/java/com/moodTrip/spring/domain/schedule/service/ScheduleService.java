package com.moodTrip.spring.domain.schedule.service;

import com.moodTrip.spring.domain.schedule.dto.request.ScheduleRequest;
import com.moodTrip.spring.domain.schedule.dto.response.ScheduleResponse;

import java.util.List;

public interface ScheduleService {
    List<ScheduleResponse> getSchedulesByRoomId(Long roomId);
    ScheduleResponse createSchedule(Long roomId, ScheduleRequest request);
    ScheduleResponse updateSchedule(Long scheduleId, ScheduleRequest request);
    void deleteSchedule(Long scheduleId);
}
