package com.moodTrip.spring.domain.schedule.controller;

import com.moodTrip.spring.domain.schedule.dto.request.ScheduleRequest;
import com.moodTrip.spring.domain.schedule.dto.response.ScheduleResponse;
import com.moodTrip.spring.domain.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class SchedulingController {
    private final ScheduleService scheduleService;

    @PostMapping("/room/{roomId}")
    public ResponseEntity<ScheduleResponse> createSchedule(@PathVariable("roomId") Long roomId, @RequestBody ScheduleRequest scheduleRequest) {
        ScheduleResponse createdSchedule = scheduleService.createSchedule(roomId, scheduleRequest);
        return ResponseEntity.ok(createdSchedule);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ScheduleResponse>> getSchedule(@PathVariable("roomId") Long roomId) {
        List<ScheduleResponse> list = scheduleService.getSchedulesByRoomId(roomId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(@PathVariable("scheduleId") Long scheduleId, @RequestBody ScheduleRequest scheduleRequest) {
        ScheduleResponse updatedSchedule = scheduleService.updateSchedule(scheduleId, scheduleRequest);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable("scheduleId") Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
}
