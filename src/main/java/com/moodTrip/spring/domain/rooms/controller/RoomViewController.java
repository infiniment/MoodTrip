package com.moodTrip.spring.domain.rooms.controller;

import com.moodTrip.spring.domain.rooms.dto.response.RoomCardDto;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/companion-rooms")
public class RoomViewController {

    private final RoomService roomService;

    // 렌더링 시작
    @GetMapping("/start")
    public String showStartPage() {
        return "creatingRoom/creatingRoom-start";
    }

    // 방 찾기 목록으로 가는 렌더링
    @GetMapping("/list")
    public String showListPage(Model model) {

        log.info("==== [EnteringRoomController] /entering-room 진입 ====");
        List<RoomCardDto> rooms = roomService.getRoomCards();
        log.info("방 개수: {}", rooms.size());
        model.addAttribute("rooms", rooms);
        return "enteringRoom/enteringRoom";
    }

    // 방 만들기로 가는 렌더링
    @GetMapping("/create")
    public String showCreatePage(@RequestParam(value = "new", required = false, defaultValue = "false") boolean isNewRoom) {
        // 서버에서 이 값을 활용 가능
        System.out.println("isNewRoom = " + isNewRoom); // 로그 출력 등
        return "creatingRoom/creatingRoom-detail";
    }

    // 방 만들 때 감정 선택하는 곳 렌더링
    @GetMapping("/emotion")
    public String showEmotionPage() {
        return "creatingRoom/choosing-emotion";
    }

    // 방 만들 때 관광지 선택하는 곳 렌더링
    @GetMapping("/attraction")
    public String showAttractionPage() {
        return "creatingRoom/choosing-attraction";
    }

    // 방 만들 때 스케줄 선택하는 곳 렌더링
    @GetMapping("/schedule")
    public String showSchedulePage() {
        return "creatingRoom/choosing-schedule";
    }

    // 방 최종 등록
    @GetMapping("/final")
    public String showFinalRegistrationPage() {
        return "creatingRoom/final-registration";
    }







}
