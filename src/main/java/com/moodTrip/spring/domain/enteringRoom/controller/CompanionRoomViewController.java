package com.moodTrip.spring.domain.enteringRoom.controller;

import com.moodTrip.spring.domain.enteringRoom.dto.response.CompanionRoomListResponse;
import com.moodTrip.spring.domain.enteringRoom.service.CompanionRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/entering-room")
@RequiredArgsConstructor
public class CompanionRoomViewController {

    private final CompanionRoomService companionRoomService;

    @GetMapping
    public String roomListPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String maxParticipants,
            @RequestParam(required = false, defaultValue = "false") Boolean urgent,
            @RequestParam(required = false, defaultValue = "default") String sort,  // 🔥 이 줄 추가!
            Model model
    ) {
        try {
            // Service에서 데이터 가져오기 (API Controller와 동일한 로직)
            List<CompanionRoomListResponse> rooms;

            if (search != null && !search.trim().isEmpty()) {
                rooms = companionRoomService.searchRooms(search);
                model.addAttribute("currentSearch", search);
            } else if (region != null && !region.trim().isEmpty()) {
                rooms = companionRoomService.getRoomsByRegion(region);
                model.addAttribute("currentRegion", region);
            } else if (maxParticipants != null && !maxParticipants.trim().isEmpty()) {
                rooms = companionRoomService.getRoomsByMaxParticipants(maxParticipants);
                model.addAttribute("currentMaxParticipants", maxParticipants);
            } else {
            rooms = companionRoomService.getAllRoomsSorted(sort);
        }

            // 마감 임박 필터 적용
            if (urgent != null && urgent) {
                rooms = rooms.stream()
                        .filter(room -> room.getUrgent())
                        .collect(java.util.stream.Collectors.toList());
            }

            // 통계 정보 계산
            long totalCount = rooms.size();
            long recruitingCount = rooms.stream()
                    .filter(room -> "모집중".equals(room.getStatus()))
                    .count();

            // Thymeleaf 템플릿에 데이터 전달
            model.addAttribute("rooms", rooms);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("recruitingCount", recruitingCount);
            model.addAttribute("currentUrgent", urgent);
            model.addAttribute("currentSort", sort);

            // 검색/필터 상태 유지를 위한 데이터
            model.addAttribute("hasFilters",
                    (search != null && !search.trim().isEmpty()) ||
                            (region != null && !region.trim().isEmpty()) ||
                            (maxParticipants != null && !maxParticipants.trim().isEmpty()) ||
                            urgent
            );
            return "enteringRoom/enteringRoom";
        } catch (Exception e) {
            model.addAttribute("rooms", java.util.Collections.emptyList());
            model.addAttribute("totalCount", 0);
            model.addAttribute("error", "방 목록을 불러오는 중 오류가 발생했습니다.");

            return "enteringRoom/enteringRoom";
        }
    }

    @GetMapping("/{roomId}/modal-data")
    @ResponseBody
    public CompanionRoomListResponse getRoomModalData(
            @PathVariable("roomId") Long roomId
    ) {
        try {
            return companionRoomService.getRoomDetailWithViewCount(roomId);

        } catch (Exception e) {
            throw new RuntimeException("존재하지 않는 방입니다.");
        }
    }

    @GetMapping("/search")
    public String searchResultsPage(
            @RequestParam("q") String query,
            Model model
    ) {
        if (query == null || query.trim().isEmpty()) {
            return "redirect:/entering-room";
        }

        try {
            List<CompanionRoomListResponse> searchResults = companionRoomService.searchRooms(query);

            model.addAttribute("rooms", searchResults);
            model.addAttribute("query", query);
            model.addAttribute("resultCount", searchResults.size());

            return "enteringRoom/searchResults";

        } catch (Exception e) {
            model.addAttribute("rooms", java.util.Collections.emptyList());
            model.addAttribute("query", query);
            model.addAttribute("resultCount", 0);
            model.addAttribute("error", "검색 중 오류가 발생했습니다.");

            return "enteringRoom/searchResults";
        }
    }

    @GetMapping("/partial")
    public String roomListPartial(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String maxParticipants,
            @RequestParam(required = false, defaultValue = "false") Boolean urgent,
            @RequestParam(required = false, defaultValue = "default") String sort,  // 🔥 이 줄 추가!
            Model model
    ) {
        // 동일한 데이터 로딩 로직 (위 roomListPage와 같음)
        try {
            List<CompanionRoomListResponse> rooms;

            if (search != null && !search.trim().isEmpty()) {
                rooms = companionRoomService.searchRooms(search);
            } else if (region != null && !region.trim().isEmpty()) {
                rooms = companionRoomService.getRoomsByRegion(region);
            } else if (maxParticipants != null && !maxParticipants.trim().isEmpty()) {
                rooms = companionRoomService.getRoomsByMaxParticipants(maxParticipants);
            } else {
                rooms = companionRoomService.getAllRoomsSorted(sort);
            }

            if (urgent != null && urgent) {
                rooms = rooms.stream()
                        .filter(room -> room.getUrgent())
                        .collect(java.util.stream.Collectors.toList());
            }

            model.addAttribute("rooms", rooms);

            return "enteringRoom/fragments/roomList :: roomList";
        } catch (Exception e) {
            model.addAttribute("rooms", java.util.Collections.emptyList());
            return "enteringRoom/fragments/roomList :: roomList";
        }
    }


}
