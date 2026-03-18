package com.moodTrip.spring.domain.rooms.controller;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.dto.response.RoomMemberResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.exception.RoomException;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.domain.rooms.enums.RoomErrorCode;
import com.moodTrip.spring.global.common.response.ApiResponse;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import com.moodTrip.spring.global.websocket.OnlineUserTracker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/room-members")
@Tag(name = "room-members API", description = "동행 방 참여, 나가기, 참여자 목록 관련 API")
public class RoomMemberController {  // 참여 / 나가기 / 참여자 목록 담당 Controller
    private final RoomService roomService;
    private final OnlineUserTracker onlineUserTracker;

    @Operation(summary = "방 참여", description = "로그인한 사용자가 해당 방에 참여합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "참여 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 참여한 사용자입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "방을 찾을 수 없습니다.")
    })
    @PostMapping("/{roomId}/join")
    public ApiResponse<Void> joinRoom(@PathVariable("roomId") Long roomId,
                                      @AuthenticationPrincipal MyUserDetails userDetails) {
        Room room = roomService.getRoomEntityById(roomId);

        Member member = userDetails.getMember();

        if (roomService.isMemberInRoom(member, room)) {
            throw new RoomException(RoomErrorCode.ROOM_MEMBER_ALREADY_EXISTS);
        }

        roomService.joinRoom(member, room, "MEMBER");
        return ApiResponse.ok();
    }

    @Operation(summary = "방 나가기", description = "로그인한 사용자가 해당 방에서 나갑니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "나가기 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "참여 정보 또는 방이 존재하지 않음")
    })
    @DeleteMapping("/{roomId}/leave")
    public ApiResponse<Void> leaveRoom(@PathVariable("roomId") Long roomId,
                                       @AuthenticationPrincipal MyUserDetails userDetails) {
        Room room = roomService.getRoomEntityById(roomId);
        Member member = userDetails.getMember();

        roomService.leaveRoom(member, room);
        return ApiResponse.ok();
    }


    @Operation(summary = "방 참여자 목록 조회", description = "해당 방에 참여 중인 모든 멤버 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 방"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/{roomId}/members")
    public ApiResponse<List<RoomMemberResponse>> getActiveMembers(@PathVariable("roomId") Long roomId,
                                                                  @AuthenticationPrincipal MyUserDetails userDetails) {
        Room room = roomService.getRoomEntityById(roomId);

        List<RoomMemberResponse> members = roomService.getActiveMembers(room);
        return ApiResponse.ok(members);
    }

    @Operation(summary = "방 온라인 참여자 목록 조회", description = "해당 방에 참여 중인 사용자 중 현재 접속 중인 멤버 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 방")
    })
    @GetMapping("/{roomId}/online-members")
    public ApiResponse<List<RoomMemberResponse>> getOnlineRoomMembers(@PathVariable("roomId") Long roomId,
                                                                      @AuthenticationPrincipal MyUserDetails userDetails) {
        Room room = roomService.getRoomEntityById(roomId);

        List<RoomMemberResponse> allMembers = roomService.getActiveMembers(room);
        List<String> onlineNicknames = onlineUserTracker.getOnlineUsers(roomId);
        List<RoomMemberResponse> onlineMembers = allMembers.stream()
                .filter(member -> onlineNicknames.contains(member.getNickname()))
                .toList();

        return ApiResponse.ok(onlineMembers);
    }

}
