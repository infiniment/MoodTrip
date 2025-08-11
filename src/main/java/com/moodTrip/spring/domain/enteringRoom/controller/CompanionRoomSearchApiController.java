package com.moodTrip.spring.domain.enteringRoom.controller;

import com.moodTrip.spring.domain.enteringRoom.dto.response.CompanionRoomListResponse;
import com.moodTrip.spring.domain.enteringRoom.service.CompanionRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Companion Room API", description = "동행자 방 관련 REST API")
@Slf4j
@RestController
@RequestMapping("/api/v1/companion-rooms/search")
@RequiredArgsConstructor
public class CompanionRoomSearchApiController {

    private final CompanionRoomService companionRoomService;

    @Operation(
            summary = "방 목록 조회",
            description = "동행자 모집방 목록을 조회합니다. 검색, 필터링 기능을 지원합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "방 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<List<CompanionRoomListResponse>> getRooms(
            @Parameter(description = "검색 키워드 (방 제목, 설명, 목적지 검색)")
            @RequestParam(required = false) String search,

            @Parameter(description = "지역 필터 (서울, 경기, 부산 등)")
            @RequestParam(required = false) String region,

            @Parameter(description = "최대 인원 필터 (2, 4, other)")
            @RequestParam(required = false) String maxParticipants,

            @Parameter(description = "마감 임박만 보기")
            @RequestParam(required = false, defaultValue = "false") Boolean urgent
    ) {
        log.info("🔍 방 목록 조회 API 호출 - search: {}, region: {}, maxParticipants: {}, urgent: {}",
                search, region, maxParticipants, urgent);

        try {
            List<CompanionRoomListResponse> rooms;

            // 🔄 요청 파라미터에 따라 다른 Service 메서드 호출
            if (search != null && !search.trim().isEmpty()) {
                // 키워드 검색
                rooms = companionRoomService.searchRooms(search);
            } else if (region != null && !region.trim().isEmpty()) {
                // 지역별 필터링
                rooms = companionRoomService.getRoomsByRegion(region);
            } else if (maxParticipants != null && !maxParticipants.trim().isEmpty()) {
                // 인원별 필터링
                rooms = companionRoomService.getRoomsByMaxParticipants(maxParticipants);
            } else {
                // 전체 목록 조회
                rooms = companionRoomService.getAllRooms();
            }

            // 🚩 마감 임박 필터 적용 (클라이언트 사이드에서도 가능하지만 서버에서 처리)
            if (urgent != null && urgent) {
                rooms = rooms.stream()
                        .filter(room -> room.getUrgent())
                        .collect(java.util.stream.Collectors.toList());
            }

            log.info("✅ 방 목록 조회 API 성공 - {}개 방 반환", rooms.size());
            return ResponseEntity.ok(rooms);

        } catch (Exception e) {
            log.error("❌ 방 목록 조회 API 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "방 상세보기",
            description = "특정 방의 상세 정보를 조회합니다. 조회 시 조회수가 증가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "방 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{room_id}")
    public ResponseEntity<CompanionRoomListResponse> getRoomDetail(
            @Parameter(description = "방 ID", example = "1")
            @PathVariable("room_id") Long roomId
    ) {
        log.info("🔍 방 상세보기 API 호출 - roomId: {}", roomId);

        try {
            // 🔍 전체 방 목록에서 특정 방 찾기 (임시 구현)
            List<CompanionRoomListResponse> allRooms = companionRoomService.getAllRooms();

            CompanionRoomListResponse room = allRooms.stream()
                    .filter(r -> r.getId().equals(roomId))
                    .findFirst()
                    .orElse(null);

            if (room == null) {
                log.warn("⚠️ 존재하지 않는 방 ID: {}", roomId);
                return ResponseEntity.notFound().build();
            }

            // 🔥 TODO: 조회수 증가 로직 추가 (추후 구현)
            log.info("✅ 방 상세보기 API 성공 - roomId: {}, title: {}", roomId, room.getTitle());
            return ResponseEntity.ok(room);

        } catch (Exception e) {
            log.error("❌ 방 상세보기 API 실패 - roomId: {}, error: {}", roomId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "방 통계 조회",
            description = "전체 방 개수, 모집중인 방 개수 등의 통계 정보를 조회합니다."
    )
    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getRoomStats() {
        log.info("📊 방 통계 조회 API 호출");

        try {
            List<CompanionRoomListResponse> allRooms = companionRoomService.getAllRooms();

            long totalCount = allRooms.size();
            long recruitingCount = allRooms.stream()
                    .filter(room -> "모집중".equals(room.getStatus()))
                    .count();
            long urgentCount = allRooms.stream()
                    .filter(room -> room.getUrgent())
                    .count();

            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalCount", totalCount);
            stats.put("recruitingCount", recruitingCount);
            stats.put("urgentCount", urgentCount);

            log.info("📊 방 통계 조회 성공 - 전체: {}, 모집중: {}, 마감임박: {}",
                    totalCount, recruitingCount, urgentCount);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("❌ 방 통계 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
