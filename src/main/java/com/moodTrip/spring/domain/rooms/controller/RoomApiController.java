package com.moodTrip.spring.domain.rooms.controller;

import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest;
import com.moodTrip.spring.domain.rooms.dto.request.UpdateRoomRequest;
import com.moodTrip.spring.domain.rooms.dto.response.RoomPageResponse;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.global.common.response.ApiResponse;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/companion-rooms")
@RequiredArgsConstructor
@Tag(name = "companion-rooms API", description = "동행 방 생성, 조회, 수정, 삭제 관련 API")
@SecurityRequirement(name = "BearerAuth")
public class RoomApiController {

    private final RoomService roomService;


    @Operation(summary = "방 생성", description = "새로운 동행 방을 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoomResponse> createRoom(@Valid @RequestBody final RoomRequest request,
                                                @AuthenticationPrincipal final MyUserDetails userDetails) {
        Long memberPk = userDetails.getMemberPk();
        RoomResponse response = roomService.createRoom(request, memberPk);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "모든 방 목록 조회", description = "전체 방 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<RoomPageResponse> getAllRooms(Pageable pageable,
                                                     @AuthenticationPrincipal final MyUserDetails userDetails) {
        return ApiResponse.ok(roomService.getAllRooms(pageable));
    }

    @Operation(summary = "방 단건 조회", description = "방 ID를 기준으로 특정 방의 정보를 조회합니다.")
    @GetMapping("/{roomId}")
    public ApiResponse<RoomResponse> getRoom(@PathVariable Long roomId,
                                             @AuthenticationPrincipal final MyUserDetails userDetails) {
        return ApiResponse.ok(roomService.getRoomById(roomId));
    }

    @Operation(summary = "방 정보 수정", description = "기존 방의 정보를 수정합니다.")
    @PatchMapping("/{roomId}")
    public ApiResponse<RoomResponse> updateRoom(@PathVariable Long roomId,
                                                @RequestBody UpdateRoomRequest request,
                                                @AuthenticationPrincipal final MyUserDetails userDetails) {
        return ApiResponse.ok(roomService.updateRoom(roomId, request, userDetails.getMemberPk()));
    }

    @Operation(summary = "방 삭제", description = "방 ID를 기준으로 해당 방을 삭제합니다.")
    @DeleteMapping("/{roomId}")
    public ApiResponse<Void> deleteRoom(@PathVariable Long roomId,
                                        @AuthenticationPrincipal final MyUserDetails userDetails) {
        roomService.deleteRoomById(roomId, userDetails.getMemberPk());
        return ApiResponse.ok();
    }
}
