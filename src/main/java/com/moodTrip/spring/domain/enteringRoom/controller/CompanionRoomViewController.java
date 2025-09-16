package com.moodTrip.spring.domain.enteringRoom.controller;

import com.moodTrip.spring.domain.enteringRoom.dto.response.CompanionRoomListResponse;
import com.moodTrip.spring.domain.enteringRoom.service.CompanionRoomService;
import com.moodTrip.spring.domain.enteringRoom.service.JoinRequestManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/entering-room")
@RequiredArgsConstructor
public class CompanionRoomViewController {

    private final CompanionRoomService companionRoomService;
    private final JoinRequestManagementService joinRequestManagementService;

    @GetMapping
    public String roomListPage(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "region", required = false) String region,
            @RequestParam(name = "maxParticipants", required = false) String maxParticipants,
            @RequestParam(name = "urgent", required = false, defaultValue = "false") Boolean urgent,
            Model model
    ) {
        try {
            log.info("방 목록 페이지 요청 - search: {}, region: {}, maxParticipants: {}, urgent: {}",
                    search, region, maxParticipants, urgent);

            // Service에서 데이터 가져오기
            List<CompanionRoomListResponse> rooms;

            if (search != null && !search.trim().isEmpty()) {
                rooms = companionRoomService.searchRooms(search);
                model.addAttribute("currentSearch", search);
                log.debug("검색 결과 - 키워드: {}, 결과수: {}", search, rooms.size());

            } else if (region != null && !region.trim().isEmpty()) {
                rooms = companionRoomService.getRoomsByRegion(region);
                model.addAttribute("currentRegion", region);
                log.debug("지역 필터 결과 - 지역: {}, 결과수: {}", region, rooms.size());

            } else if (maxParticipants != null && !maxParticipants.trim().isEmpty()) {
                rooms = companionRoomService.getRoomsByMaxParticipants(maxParticipants);
                model.addAttribute("currentMaxParticipants", maxParticipants);
                log.debug("인원 필터 결과 - 인원: {}, 결과수: {}", maxParticipants, rooms.size());

            } else {
                // 🔥 수정: 기본 메서드 호출
                rooms = companionRoomService.getAllRooms();
                log.debug("전체 목록 조회 - 결과수: {}", rooms.size());
            }

            // 마감 임박 필터 적용
            if (urgent != null && urgent) {
                int beforeFilterCount = rooms.size();
                rooms = rooms.stream()
                        .filter(room -> room.getUrgent())
                        .collect(java.util.stream.Collectors.toList());
                log.debug("마감임박 필터 적용 - 필터 전: {}, 필터 후: {}", beforeFilterCount, rooms.size());
            }

            // 통계 정보 계산
            long totalCount = rooms.size();
            long recruitingCount = rooms.stream()
                    .filter(room -> "모집중".equals(room.getStatus()))
                    .count();

            // ✅ 헤더 배지용 데이터 추가
            try {
                Integer totalPendingRequests = joinRequestManagementService.getTotalPendingRequestsForSidebar();
                model.addAttribute("totalPendingRequests", totalPendingRequests);
            } catch (Exception e) {
                model.addAttribute("totalPendingRequests", 0);
            }

            // Thymeleaf 템플릿에 데이터 전달
            model.addAttribute("rooms", rooms);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("recruitingCount", recruitingCount);
            model.addAttribute("currentUrgent", urgent);

            // 검색/필터 상태 유지를 위한 데이터
            model.addAttribute("hasFilters",
                    (search != null && !search.trim().isEmpty()) ||
                            (region != null && !region.trim().isEmpty()) ||
                            (maxParticipants != null && !maxParticipants.trim().isEmpty()) ||
                            urgent
            );

            log.info("방 목록 페이지 로드 완료 - 총 {}개 방, 모집중 {}개", totalCount, recruitingCount);
            return "enteringRoom/enteringRoom";

        } catch (Exception e) {
            log.error("방 목록 페이지 로드 실패", e);

            model.addAttribute("rooms", java.util.Collections.emptyList());
            model.addAttribute("totalCount", 0);
            model.addAttribute("recruitingCount", 0);
            model.addAttribute("error", "방 목록을 불러오는 중 오류가 발생했습니다.");

            // ✅ 예외 상황에도 기본값 주입
            model.addAttribute("totalPendingRequests", 0);

            return "enteringRoom/enteringRoom";
        }
    }

    @GetMapping("/{roomId}/modal-data")
    @ResponseBody
    public CompanionRoomListResponse getRoomModalData(
            @PathVariable("roomId") Long roomId
    ) {
        try {
            log.info("방 상세 모달 데이터 요청 - roomId: {}", roomId);

            CompanionRoomListResponse response = companionRoomService.getRoomDetailWithViewCount(roomId);

            log.info("방 상세 모달 데이터 반환 완료 - roomId: {}, title: {}", roomId, response.getTitle());
            return response;

        } catch (Exception e) {
            log.error("방 상세 데이터 조회 실패 - roomId: {}", roomId, e);
            throw new RuntimeException("존재하지 않는 방입니다.");
        }
    }

    @GetMapping("/search")
    public String searchResultsPage(
            @RequestParam("query") String query,
            Model model
    ) {
        if (query == null || query.trim().isEmpty()) {
            return "redirect:/entering-room";
        }

        try {
            log.info("검색 결과 페이지 요청 - query: {}", query);

            List<CompanionRoomListResponse> searchResults = companionRoomService.searchRooms(query);

            // ✅ 헤더 배지용 데이터 추가
            try {
                Integer totalPendingRequests = joinRequestManagementService.getTotalPendingRequestsForSidebar();
                model.addAttribute("totalPendingRequests", totalPendingRequests);
            } catch (Exception e) {
                model.addAttribute("totalPendingRequests", 0);
            }

            model.addAttribute("rooms", searchResults);
            model.addAttribute("query", query);
            model.addAttribute("resultCount", searchResults.size());

            log.info("검색 결과 - 키워드: {}, 결과수: {}", query, searchResults.size());
            return "enteringRoom/searchResults";

        } catch (Exception e) {
            log.error("검색 실패 - query: {}", query, e);

            model.addAttribute("rooms", java.util.Collections.emptyList());
            model.addAttribute("query", query);
            model.addAttribute("resultCount", 0);
            model.addAttribute("error", "검색 중 오류가 발생했습니다.");
            model.addAttribute("totalPendingRequests", 0); // ✅ 기본값

            return "enteringRoom/searchResults";
        }
    }

    @GetMapping("/partial")
    public String roomListPartial(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "region", required = false) String region,
            @RequestParam(name = "maxParticipants", required = false) String maxParticipants,
            @RequestParam(name = "urgent", required = false, defaultValue = "false") Boolean urgent,
            Model model
    ) {
        try {
            log.debug("부분 렌더링 요청 - search: {}, region: {}, maxParticipants: {}, urgent: {}",
                    search, region, maxParticipants, urgent);

            List<CompanionRoomListResponse> rooms;

            if (search != null && !search.trim().isEmpty()) {
                rooms = companionRoomService.searchRooms(search);

            } else if (region != null && !region.trim().isEmpty()) {
                rooms = companionRoomService.getRoomsByRegion(region);

            } else if (maxParticipants != null && !maxParticipants.trim().isEmpty()) {
                rooms = companionRoomService.getRoomsByMaxParticipants(maxParticipants);

            } else {
                // 🔥 수정: 기본 메서드 호출
                rooms = companionRoomService.getAllRooms();
            }

            // 마감 임박 필터 적용
            if (urgent != null && urgent) {
                rooms = rooms.stream()
                        .filter(room -> room.getUrgent())
                        .collect(java.util.stream.Collectors.toList());
            }

            model.addAttribute("rooms", rooms);

            // ✅ 부분 렌더링에도 배지 값 기본 주입
            try {
                Integer totalPendingRequests = joinRequestManagementService.getTotalPendingRequestsForSidebar();
                model.addAttribute("totalPendingRequests", totalPendingRequests);
            } catch (Exception e) {
                model.addAttribute("totalPendingRequests", 0);
            }

            log.debug("부분 렌더링 완료 - 결과수: {}", rooms.size());
            return "enteringRoom/fragments/roomList :: roomList";

        } catch (Exception e) {
            log.error("부분 렌더링 실패", e);

            model.addAttribute("rooms", java.util.Collections.emptyList());
            model.addAttribute("totalPendingRequests", 0); // ✅ 기본값
            return "enteringRoom/fragments/roomList :: roomList";
        }
    }
}
