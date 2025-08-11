package com.moodTrip.spring.domain.enteringRoom.controller;

import com.moodTrip.spring.domain.enteringRoom.dto.response.CompanionRoomListResponse;
import com.moodTrip.spring.domain.enteringRoom.service.CompanionRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Companion Room Search API", description = "동행자 방 검색/조회 관련 REST API")
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
        try {
            List<CompanionRoomListResponse> rooms;

            // 서비스 호출하기
            if (search != null && !search.trim().isEmpty()) {
                rooms = companionRoomService.searchRooms(search);
            } else if (region != null && !region.trim().isEmpty()) {
                rooms = companionRoomService.getRoomsByRegion(region);
            } else if (maxParticipants != null && !maxParticipants.trim().isEmpty()) {
                rooms = companionRoomService.getRoomsByMaxParticipants(maxParticipants);
            } else {
                rooms = companionRoomService.getAllRooms();
            }

            // 마감 임박 필터 적용
            if (urgent != null && urgent) {
                rooms = rooms.stream()
                        .filter(room -> room.getUrgent())
                        .collect(java.util.stream.Collectors.toList());
            }

            return ResponseEntity.ok(rooms);

        } catch (Exception e) {
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

        try {
            // 전체 방 목록에서 특정 방 찾기 (임시 구현)
            List<CompanionRoomListResponse> allRooms = companionRoomService.getAllRooms();

            CompanionRoomListResponse room = allRooms.stream()
                    .filter(r -> r.getId().equals(roomId))
                    .findFirst()
                    .orElse(null);

            if (room == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(room);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}