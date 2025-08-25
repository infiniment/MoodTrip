package com.moodTrip.spring.domain.mypageRoom.controller;

import com.moodTrip.spring.domain.enteringRoom.service.JoinRequestManagementService;
import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.ProfileService;
import com.moodTrip.spring.domain.mypageRoom.dto.response.CreatedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.dto.response.JoinedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.service.MypageRoomService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageViewController {

    private final SecurityUtil securityUtil;
    private final ProfileService profileService;
    private final MypageRoomService mypageRoomService;
    private final JoinRequestManagementService joinRequestManagementService;

    // 기본정보 프로필 페이지
    @GetMapping("/my-profile")
    public String viewMyProfile(Model model) {
        Member currentMember = securityUtil.getCurrentMember();
        ProfileResponse profile = profileService.getMyProfile(currentMember);

        model.addAttribute("profile", profile);
        model.addAttribute("currentMember", currentMember);
        model.addAttribute("pageTitle", "내 정보");
        model.addAttribute("isLoggedIn", true);

        // 사이드바 배지용 데이터 추가
        try {
            Integer totalPendingRequests = joinRequestManagementService.getTotalPendingRequestsForSidebar();
            model.addAttribute("totalPendingRequests", totalPendingRequests);
        } catch (Exception e) {
            model.addAttribute("totalPendingRequests", 0);
        }

        return "mypage/my-profile";
    }

    // 마이페이지 매칭 정보 페이지 렌더링
    @GetMapping("/my-matching")
    public String myMatching(
            @RequestParam(name = "activeTab", required = false) String activeTab,
            @RequestParam(name = "tab",       required = false) String legacyTab,
            Model model
    ) {
        String tab = (activeTab != null ? activeTab : legacyTab);
        Member currentMember = securityUtil.getCurrentMember();

        String validatedTab = validateAndNormalizeTab(tab);

        // 기존 로직...
        List<JoinedRoomResponse> joinedRooms = null;
        List<CreatedRoomResponse> createdRooms = null;

        if ("received".equals(validatedTab)) {
            joinedRooms = mypageRoomService.getMyJoinedRooms(currentMember);
        } else if ("created".equals(validatedTab)) {
            createdRooms = mypageRoomService.getMyCreatedRooms(currentMember);
        }

        // 사이드바 배지용 데이터 추가
        try {
            Integer totalPendingRequests = joinRequestManagementService.getTotalPendingRequestsForSidebar();
            model.addAttribute("totalPendingRequests", totalPendingRequests);
        } catch (Exception e) {
            model.addAttribute("totalPendingRequests", 0);
        }

        setupModelAttributes(model, validatedTab, currentMember, joinedRooms, createdRooms);
        return "mypage/my-matching";
    }

    private String validateAndNormalizeTab(String tab) {
        if (tab == null || tab.trim().isEmpty()) {
            return "received";
        }
        // 공백 제거 후 소문자 변환
        String normalizedTab = tab.trim().toLowerCase();

        switch (normalizedTab) {
            case "received":
            case "joined":  // "joined"도 "received"로 처리 (동일한 의미)
                return "received";

            case "created":
            case "created-rooms":  // 다양한 변형 처리
                return "created";

            default:
                return "received";
        }
    }
    // 템플릿에서 th:classappend="${activeTab == 'received' ? 'active' : ''}" 가 작동하도록 함
    private void setupModelAttributes(Model model, String activeTab, Member currentMember,
                                      List<JoinedRoomResponse> joinedRooms,
                                      List<CreatedRoomResponse> createdRooms) {

        model.addAttribute("activeTab", activeTab);

        // 기본 페이지 정보
        model.addAttribute("pageTitle", "매칭 정보");
        model.addAttribute("currentMember", currentMember);
        model.addAttribute("isLoggedIn", true);

        // 방 데이터 전달
        model.addAttribute("joinedRooms", joinedRooms != null ? joinedRooms : List.of());
        model.addAttribute("createdRooms", createdRooms != null ? createdRooms : List.of());

        // 빈 상태 체크용 boolean (HTML에서 조건부 렌더링에 사용)
        model.addAttribute("hasJoinedRooms", joinedRooms != null && !joinedRooms.isEmpty());
        model.addAttribute("hasCreatedRooms", createdRooms != null && !createdRooms.isEmpty());
    }

    // 마이페이지의 메인으로 리다이렉트
    @GetMapping
    public String mypageMain() {
        return "redirect:/mypage/my-profile";
    }

}