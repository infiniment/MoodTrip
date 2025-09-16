package com.moodTrip.spring.domain.schedule.controller;

import com.moodTrip.spring.domain.schedule.dto.request.ScheduleRequest;
import com.moodTrip.spring.domain.schedule.dto.response.ScheduleResponse;
import com.moodTrip.spring.domain.schedule.dto.response.ScheduleWebSocketMessage;
import com.moodTrip.spring.domain.rooms.service.RoomAuthService;
import com.moodTrip.spring.domain.schedule.service.ScheduleService;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class SchedulingController {
    private final ScheduleService scheduleService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomAuthService roomAuthService;



    @PostMapping("/room/{roomId}")
    public ResponseEntity<ScheduleResponse> createSchedule(@PathVariable("roomId") Long roomId, @AuthenticationPrincipal MyUserDetails userDetails, @RequestBody ScheduleRequest scheduleRequest) {
        // 현재 로그인한 사용자 PK 가져오기
        Long memberPk = userDetails.getMember().getMemberPk();

        // 방 멤버인지 검사
        roomAuthService.assertActiveMember(roomId, memberPk);

        // 일정 생성
        ScheduleResponse createdSchedule = scheduleService.createSchedule(roomId, scheduleRequest);

        // WebSocket 알림
        messagingTemplate.convertAndSend(
                "/sub/schedule/room/" + roomId,
                new ScheduleWebSocketMessage("CREATE", createdSchedule)
        );

        return ResponseEntity.ok(createdSchedule);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ScheduleResponse>> getSchedule(@PathVariable("roomId") Long roomId,
                                                              @AuthenticationPrincipal MyUserDetails userDetails) {
        Long memberPk = userDetails.getMember().getMemberPk();
        roomAuthService.assertActiveMember(roomId, memberPk);

        List<ScheduleResponse> list = scheduleService.getSchedulesByRoomId(roomId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(@PathVariable("scheduleId") Long scheduleId,  @AuthenticationPrincipal MyUserDetails userDetails, @RequestBody ScheduleRequest scheduleRequest) {
        Long roomId = scheduleService.getRoomIdByScheduleId(scheduleId);
        Long memberPk = userDetails.getMember().getMemberPk();

        roomAuthService.assertActiveMember(roomId, memberPk);

        ScheduleResponse updatedSchedule = scheduleService.updateSchedule(scheduleId, scheduleRequest);

        messagingTemplate.convertAndSend(
                "/sub/schedule/room/" + roomId,
                new ScheduleWebSocketMessage("UPDATE", updatedSchedule)
        );

        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable("scheduleId") Long scheduleId, @AuthenticationPrincipal MyUserDetails userDetails) {
        Long roomId = scheduleService.getRoomIdByScheduleId(scheduleId);
        Long memberPk = userDetails.getMember().getMemberPk();
        roomAuthService.assertActiveMember(roomId, memberPk);

        scheduleService.deleteSchedule(scheduleId);


        messagingTemplate.convertAndSend(
                "/sub/schedule/room/" + roomId,
                new ScheduleWebSocketMessage("DELETE", scheduleId)
        );

        return ResponseEntity.noContent().build();
    }
}
