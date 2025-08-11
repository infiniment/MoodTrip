package com.moodTrip.spring.domain.rooms.controller;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest;
import com.moodTrip.spring.domain.rooms.dto.request.UpdateRoomRequest;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companion-rooms")
@Tag(name = "companion-rooms API", description = "동행 방 생성, 조회, 수정, 삭제 관련 API")
@SecurityRequirement(name = "BearerAuth")
public class RoomApiController { // CRUD 담당 Controller

    private final RoomService roomService;


    @Operation(summary = "방 생성", description = "새로운 동행 방을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "방 생성 성공")
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@RequestBody final RoomRequest request,
                                                   @AuthenticationPrincipal final MyUserDetails userDetails) {
        Member member = userDetails.getMember();
        Long memberPk = member.getMemberPk();
        RoomResponse response = roomService.createRoom(request, memberPk);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "모든 방 목록 조회", description = "전체 방 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "방 생성 성공")
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @Operation(summary = "방 단건 조회", description = "방 ID를 기준으로 특정 방의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "방 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방")
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @Operation(summary = "방 정보 수정", description = "기존 방의 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "방 수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방")
    })
    @PatchMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable("roomId") Long roomId, @RequestBody UpdateRoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, request));
    }

    @Operation(summary = "방 삭제", description = "방 ID를 기준으로 해당 방을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "방 삭제 성공"), // soft delete
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방")
    })
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("roomId") Long roomId) {
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build(); // 204 반환(반환 타입 없는 경우 204)
    }
}
