package com.moodTrip.spring.domain.enteringRoom.controller;

import com.moodTrip.spring.domain.enteringRoom.dto.response.CompanionRoomListResponse;
import com.moodTrip.spring.domain.enteringRoom.service.CompanionRoomService;
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

    @GetMapping
    public String roomListPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String maxParticipants,
            @RequestParam(required = false, defaultValue = "false") Boolean urgent,
            Model model
    ) {
        log.info("🖼️ 방 목록 페이지 요청 - search: {}, region: {}, maxParticipants: {}, urgent: {}",
                search, region, maxParticipants, urgent);

        try {
            // 🔄 Service에서 데이터 가져오기 (API Controller와 동일한 로직)
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
                rooms = companionRoomService.getAllRooms();
            }

            // 마감 임박 필터 적용
            if (urgent != null && urgent) {
                rooms = rooms.stream()
                        .filter(room -> room.getUrgent())
                        .collect(java.util.stream.Collectors.toList());
            }

            // 📊 통계 정보 계산
            long totalCount = rooms.size();
            long recruitingCount = rooms.stream()
                    .filter(room -> "모집중".equals(room.getStatus()))
                    .count();

            // 🎨 Thymeleaf 템플릿에 데이터 전달
            model.addAttribute("rooms", rooms);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("recruitingCount", recruitingCount);
            model.addAttribute("currentUrgent", urgent);

            // 🔍 검색/필터 상태 유지를 위한 데이터
            model.addAttribute("hasFilters",
                    (search != null && !search.trim().isEmpty()) ||
                            (region != null && !region.trim().isEmpty()) ||
                            (maxParticipants != null && !maxParticipants.trim().isEmpty()) ||
                            urgent
            );

            log.info("✅ 방 목록 페이지 렌더링 완료 - {}개 방 표시", rooms.size());

            // 🎯 Thymeleaf 템플릿 반환
            return "entering-room/list";

        } catch (Exception e) {
            log.error("❌ 방 목록 페이지 렌더링 실패: {}", e.getMessage(), e);

            // 🚨 에러 페이지로 리다이렉트 또는 빈 목록 표시
            model.addAttribute("rooms", java.util.Collections.emptyList());
            model.addAttribute("totalCount", 0);
            model.addAttribute("error", "방 목록을 불러오는 중 오류가 발생했습니다.");

            return "entering-room/list";
        }
    }

    @GetMapping("/{roomId}/modal-data")
    @ResponseBody  // JSON 응답 반환
    public CompanionRoomListResponse getRoomModalData(
            @PathVariable("roomId") Long roomId
    ) {
        log.info("🔍 방 상세보기 모달 데이터 요청 - roomId: {}", roomId);

        try {
            // 전체 방 목록에서 해당 방 찾기
            List<CompanionRoomListResponse> allRooms = companionRoomService.getAllRooms();

            CompanionRoomListResponse room = allRooms.stream()
                    .filter(r -> r.getId().equals(roomId))
                    .findFirst()
                    .orElse(null);

            if (room == null) {
                log.warn("⚠️ 존재하지 않는 방 - roomId: {}", roomId);
                throw new RuntimeException("존재하지 않는 방입니다.");
            }

            // 🔥 TODO: 조회수 증가 로직 (모달을 열 때마다 +1)
            log.info("✅ 방 상세보기 모달 데이터 반환 완료 - roomId: {}, title: {}",
                    roomId, room.getTitle());

            return room;

        } catch (Exception e) {
            log.error("❌ 방 상세보기 모달 데이터 조회 실패 - roomId: {}, error: {}",
                    roomId, e.getMessage(), e);
            throw e; // 예외를 다시 던져서 프론트엔드에서 처리하도록
        }
    }

    @GetMapping("/search")
    public String searchResultsPage(
            @RequestParam("q") String query,
            Model model
    ) {
        log.info("🔍 검색 결과 페이지 요청 - query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            return "redirect:/entering-room";
        }

        try {
            List<CompanionRoomListResponse> searchResults = companionRoomService.searchRooms(query);

            model.addAttribute("rooms", searchResults);
            model.addAttribute("query", query);
            model.addAttribute("resultCount", searchResults.size());

            log.info("✅ 검색 결과 페이지 렌더링 완료 - '{}'로 {}개 결과", query, searchResults.size());

            return "entering-room/search-results";

        } catch (Exception e) {
            log.error("❌ 검색 결과 페이지 렌더링 실패 - query: {}, error: {}", query, e.getMessage(), e);

            model.addAttribute("rooms", java.util.Collections.emptyList());
            model.addAttribute("query", query);
            model.addAttribute("resultCount", 0);
            model.addAttribute("error", "검색 중 오류가 발생했습니다.");

            return "entering-room/search-results";
        }
    }

    @GetMapping("/partial")
    public String roomListPartial(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String maxParticipants,
            @RequestParam(required = false, defaultValue = "false") Boolean urgent,
            Model model
    ) {
        log.info("🔄 부분 업데이트 요청 - search: {}, region: {}, maxParticipants: {}",
                search, region, maxParticipants);

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
                rooms = companionRoomService.getAllRooms();
            }

            if (urgent != null && urgent) {
                rooms = rooms.stream()
                        .filter(room -> room.getUrgent())
                        .collect(java.util.stream.Collectors.toList());
            }

            model.addAttribute("rooms", rooms);

            // 🎯 부분 템플릿만 렌더링
            return "entering-room/fragments/room-list :: roomList";

        } catch (Exception e) {
            log.error("❌ 부분 업데이트 실패: {}", e.getMessage(), e);
            model.addAttribute("rooms", java.util.Collections.emptyList());
            return "entering-room/fragments/room-list :: roomList";
        }
    }
}