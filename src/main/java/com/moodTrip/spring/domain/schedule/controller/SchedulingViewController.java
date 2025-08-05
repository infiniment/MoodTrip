package com.moodTrip.spring.domain.schedule.controller;

import com.moodTrip.spring.domain.rooms.dto.response.RoomMemberResponse;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/scheduling")
public class SchedulingViewController  {
    private final RoomService roomService;

    @GetMapping("/{roomId}")
    public String showSchedulingPage(@PathVariable("roomId") Long roomId,
                                     @AuthenticationPrincipal MyUserDetails userDetails,
                                     Model model) {
        // 로그인 사용자
        var currentMember = userDetails.getMember();

        // 1. 방 정보 조회
        Room room = roomService.getRoomEntityById(roomId);
        RoomResponse roomResponse = roomService.toResponseDto(room);

        // 2. 참여자 목록
        List<RoomMemberResponse> members = roomService.getActiveMembers(room);

        // 3. 여행 일수 계산
        LocalDate start = room.getTravelStartDate();
        LocalDate end = room.getTravelEndDate();
        long days = ChronoUnit.DAYS.between(start, end);
        String durationLabel = (days == 0) ? "당일치기" : "(" + days + "박" + (days + 1) + "일)";

        // 4. 모델에 값 추가
        model.addAttribute("room", roomResponse);
        model.addAttribute("members", members);
        model.addAttribute("roomId", roomId);
        model.addAttribute("durationLabel", durationLabel);
        model.addAttribute("currentMember", currentMember);

        return "schedule-with-companion/scheduling";
    }
}