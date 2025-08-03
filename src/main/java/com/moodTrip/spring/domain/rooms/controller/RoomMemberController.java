package com.moodTrip.spring.domain.rooms.controller;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.dto.response.RoomMemberResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.global.common.exception.CustomException;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.moodTrip.spring.global.common.code.status.ErrorStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/room-members")
@Tag(name = "room-members API", description = "동행 방 참여, 나가기, 참여자 목록 관련 API")
public class RoomMemberController {  // 참여 / 나가기 / 참여자 목록 담당 Controller
    private final RoomService roomService;
    private final RoomRepository roomRepository;

    @Operation(summary = "방 참여", description = "로그인한 사용자가 해당 방에 참여합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "참여 성공"),
            @ApiResponse(responseCode = "409", description = "이미 참여한 사용자입니다."),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없습니다.")
    })
    @PostMapping("/{roomId}/join")
    public ResponseEntity<Void> joinRoom(@PathVariable("roomId") Long roomId,
                                         @AuthenticationPrincipal MyUserDetails userDetails) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new CustomException(ROOM_NOT_FOUND));

        Member member = userDetails.getMember();

        if (roomService.isMemberInRoom(member, room)) {
            throw new CustomException(ROOM_MEMBER_ALREADY_EXISTS);
        }

        roomService.joinRoom(member, room, "MEMBER");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "방 나가기", description = "로그인한 사용자가 해당 방에서 나갑니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "나가기 성공"),
            @ApiResponse(responseCode = "404", description = "참여 정보 또는 방이 존재하지 않음")
    })
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<Room> leaveRoom(@PathVariable("roomId") Long roomId,
                                          @AuthenticationPrincipal MyUserDetails userDetails) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new CustomException(ROOM_NOT_FOUND));
        Member member = userDetails.getMember();

        roomService.leaveRoom(member, room);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "방 참여자 목록 조회", description = "해당 방에 참여 중인 모든 멤버 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/{roomId}/members")
    public ResponseEntity<List<RoomMemberResponse>> getActiveMembers(@PathVariable("roomId") Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new CustomException(ROOM_NOT_FOUND));

        List<RoomMemberResponse> members = roomService.getActiveMembers(room);
        return ResponseEntity.ok(members);
    }

}
