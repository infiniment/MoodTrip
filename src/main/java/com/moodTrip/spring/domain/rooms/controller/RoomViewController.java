package com.moodTrip.spring.domain.rooms.controller;

import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.emotion.dto.response.EmotionCategoryDto;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.domain.rooms.dto.response.RoomCardDto;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/companion-rooms")
public class RoomViewController {

    private final RoomService roomService;
    private final AttractionService attractionService;
    private final EmotionService emotionService;


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
    public String showCreatePage(@RequestParam(value = "new", required = false, defaultValue = "false") boolean isNewRoom,
                                 @AuthenticationPrincipal MyUserDetails user) {
        if (user == null) return "redirect:/login?redirect=" + url("/companion-rooms/create?new=" + isNewRoom);
        return "creatingRoom/creatingRoom-detail";
    }

    // 방 만들 때 감정 선택하는 곳 렌더링
    @GetMapping("/emotion")
    public String showEmotionPage(@AuthenticationPrincipal MyUserDetails user, Model model) {
        if (user == null) return "redirect:/login?redirect=" + url("/companion-rooms/emotion");

        // EmotionService 통해 감정 데이터 가져오기
        List<EmotionCategoryDto> emotionCategories = emotionService.getEmotionCategories();
        model.addAttribute("emotionCategories", emotionCategories);

        return "creatingRoom/choosing-emotion";
    }

    // 방 만들 때 관광지 선택하는 곳 렌더링
    @GetMapping("/attraction")
    public String showAttractionPage(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "areaCode", required = false) Integer areaCode,
            @RequestParam(value = "sigunguCode", required = false) Integer sigunguCode,
            @RequestParam(value = "contentTypeId", required = false) Integer contentTypeId,
            // SSR 초기 페이지/사이즈 (기본 0, 9개 = 3x3)
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            @AuthenticationPrincipal MyUserDetails user,
            Model model
    ) {
        if (user == null) {
            return "redirect:/login?redirect=" + url(buildAttractionPath(q, areaCode, sigunguCode, contentTypeId));
        }

        // 로그인/검색어 바인딩
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("q", q == null ? "" : q);

        // ★ 통합 검색 + 페이지네이션 (제목 앞글자 우선 정렬)
        var pageResult = attractionService
                .searchKeywordPrefTitleStarts(q, areaCode, sigunguCode, contentTypeId, page, size);

        var attractions = pageResult.map(AttractionResponse::from).getContent();

        model.addAttribute("attractions", attractions);
        model.addAttribute("page", pageResult.getNumber());            // 현재 페이지(0-base)
        model.addAttribute("size", pageResult.getSize());              // 페이지 크기
        model.addAttribute("totalPages", pageResult.getTotalPages());  // 전체 페이지 수
        model.addAttribute("totalElements", pageResult.getTotalElements()); // 전체 아이템 수

        return "creatingRoom/choosing-attraction";
    }

    // 방 만들 때 스케줄 선택하는 곳 렌더링
    @GetMapping("/schedule")
    public String showSchedulePage(@AuthenticationPrincipal MyUserDetails user) {
        if (user == null) return "redirect:/login?redirect=" + url("/companion-rooms/schedule");
        return "creatingRoom/choosing-schedule";
    }


    // 방 최종 등록
    @GetMapping("/final")
    public String showFinalRegistrationPage(@AuthenticationPrincipal MyUserDetails user) {
        if (user == null) return "redirect:/login?redirect=" + url("/companion-rooms/final");
        return "creatingRoom/final-registration";
    }

    private static String url(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }

    private static String buildAttractionPath(String q, Integer areaCode, Integer sigunguCode, Integer contentTypeId) {
        StringBuilder sb = new StringBuilder("/companion-rooms/attraction");
        String sep = "?";
        if (q != null && !q.isBlank()) { sb.append(sep).append("q=").append(url(q)); sep="&"; }
        if (areaCode != null) { sb.append(sep).append("areaCode=").append(areaCode); sep="&"; }
        if (sigunguCode != null) { sb.append(sep).append("sigunguCode=").append(sigunguCode); sep="&"; }
        if (contentTypeId != null) { sb.append(sep).append("contentTypeId=").append(contentTypeId); }
        return sb.toString();
    }





}
