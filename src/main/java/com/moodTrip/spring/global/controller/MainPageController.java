package com.moodTrip.spring.global.controller;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 🌟 메인 페이지 및 인증 관련 페이지를 담당하는 컨트롤러
 *
 * ✅ 기능:
 * - 메인페이지 렌더링 (로그인 상태별 조건부 헤더)
 * - 로그인/회원가입 페이지 렌더링
 */
@Controller
@RequiredArgsConstructor
public class MainPageController {

    private final SecurityUtil securityUtil;
    private final ProfileRepository profileRepository;
    private final RoomService roomService;


    @GetMapping("/")
    public String mainPage(Model model) {

        boolean loggedIn = false;

        if (securityUtil.isAuthenticated()) {
            Member currentMember = securityUtil.getCurrentMember();
            if (currentMember != null) {
                loggedIn = true;
                model.addAttribute("currentMember", currentMember);
                model.addAttribute("userNickname", currentMember.getNickname());
                model.addAttribute("userEmail", currentMember.getEmail());
                profileRepository.findByMember(currentMember)
                        .ifPresent(profile -> model.addAttribute("profileImage", profile.getProfileImage()));
            }
        }


        List<RoomResponse> rooms = roomService.getAllRooms();
        model.addAttribute("rooms", rooms);
        model.addAttribute("isLoggedIn", loggedIn);

        return "mainpage/mainpage";  // Thymeleaf 템플릿 위치

    }

}
