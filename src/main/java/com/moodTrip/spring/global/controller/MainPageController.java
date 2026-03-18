package com.moodTrip.spring.global.controller;

import com.moodTrip.spring.domain.enteringRoom.service.JoinRequestManagementService;
import com.moodTrip.spring.domain.enteringRoom.service.JoinRequestManagementService;
import com.moodTrip.spring.domain.mainpage.dto.response.MainPageRoomResponse;
import com.moodTrip.spring.domain.mainpage.service.MainPageService;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import com.moodTrip.spring.domain.weather.dto.response.MainPageWeatherAttractionResponse;
import com.moodTrip.spring.domain.weather.service.WeatherAttractionService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainPageController {

    private final SecurityUtil securityUtil;
    private final ProfileRepository profileRepository;
    private final JoinRequestManagementService joinRequestManagementService;
    private final MainPageService mainPageService;
    private final WeatherAttractionService weatherAttractionService;

    @GetMapping("/")
    public String mainPage(HttpServletRequest request, Model model) {
        log.info("메인페이지 요청 시작");

        boolean loggedIn = false;
        Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
        model.addAttribute("isAdmin", isAdmin);

        if (securityUtil.isAuthenticated()) {
            Member currentMember = securityUtil.getCurrentMember();
            if (currentMember != null) {
                loggedIn = true;
                model.addAttribute("currentMember", currentMember);
                model.addAttribute("userNickname", currentMember.getNickname());
                model.addAttribute("userEmail", currentMember.getEmail());

                profileRepository.findByMember(currentMember)
                        .ifPresent(profile -> model.addAttribute("profileImage", profile.getProfileImage()));

                try {
                    Integer totalPendingRequests = joinRequestManagementService.getTotalPendingRequestsForSidebar();
                    model.addAttribute("totalPendingRequests", totalPendingRequests);
                    log.debug("사이드바 배지 데이터 설정 완료 - 대기 중인 요청: {}건", totalPendingRequests);
                } catch (Exception e) {
                    log.warn("사이드바 배지 데이터 조회 실패", e);
                    model.addAttribute("totalPendingRequests", 0);
                }
            }
        }

        model.addAttribute("isLoggedIn", loggedIn);

        try {
            List<MainPageRoomResponse> popularRooms = mainPageService.getPopularRooms();
            model.addAttribute("popularRooms", popularRooms);
            model.addAttribute("roomCount", popularRooms.size());

            log.info("메인페이지 인기방 데이터 로드 완료 - 로그인: {}, 인기방: {}개",
                    loggedIn, popularRooms.size());

        } catch (Exception e) {
            log.error("인기 방 데이터 조회 실패", e);
            model.addAttribute("popularRooms", List.of());
            model.addAttribute("roomCount", 0);
            model.addAttribute("roomError", "방 목록을 불러오는 중 오류가 발생했습니다.");
        }

        try {
            List<MainPageWeatherAttractionResponse> weatherRecommendations =
                    weatherAttractionService.getMainPageWeatherRecommendations();

            model.addAttribute("weatherRecommendations", weatherRecommendations);
            model.addAttribute("weatherCount", weatherRecommendations.size());

            log.info("메인페이지 날씨 추천 데이터 로드 완료 - {}개", weatherRecommendations.size());

        } catch (Exception e) {
            log.error("날씨별 여행지 추천 데이터 조회 실패", e);
            model.addAttribute("weatherRecommendations", List.of());
            model.addAttribute("weatherCount", 0);
            model.addAttribute("weatherError", "날씨별 추천을 불러오는 중 오류가 발생했습니다.");
        }

        return "mainpage/mainpage";
    }
}
