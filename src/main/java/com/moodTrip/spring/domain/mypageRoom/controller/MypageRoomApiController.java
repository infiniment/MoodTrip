package com.moodTrip.spring.domain.mypageRoom.controller;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.mypageRoom.dto.response.CreatedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.dto.response.JoinedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.service.MypageRoomService;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

// 마이페이지 방 관리 관련 api 컨트롤러
@Slf4j
@RestController
@RequestMapping("/api/v1/mypage/rooms")
@RequiredArgsConstructor
public class MypageRoomApiController {

    private final MypageRoomService mypageRoomService;
    private final SecurityUtil securityUtil;

    // 내가 입장한 방 목록 조회
    @GetMapping("/joined")
    public ResponseEntity<List<JoinedRoomResponse>> getMyJoinedRooms() {
        Member currentMember = securityUtil.getCurrentMember();

        // 서비스에서 내가 참여한 방 목록 조회
        List<JoinedRoomResponse> joinedRooms = mypageRoomService.getMyJoinedRooms(currentMember);

        return ResponseEntity.ok(joinedRooms);
    }

    // 내가 만든 방 목록 조회
    @GetMapping("/created")
    public ResponseEntity<List<CreatedRoomResponse>> getMyCreatedRooms() {
        Member currentMember = securityUtil.getCurrentMember();

        List<CreatedRoomResponse> createdRooms = mypageRoomService.getMyCreatedRooms(currentMember);

        return ResponseEntity.ok(createdRooms);
    }

    // 방 삭제하기
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("roomId") Long roomId) throws AccessDeniedException {
        Member currentMember = securityUtil.getCurrentMember();
        mypageRoomService.deleteRoom(roomId, currentMember);
        return ResponseEntity.noContent().build();
    }

    // 방 나가기
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(@PathVariable("roomId") Long roomId) {
        Member currentMember = securityUtil.getCurrentMember();
        mypageRoomService.leaveRoom(roomId, currentMember);
        return ResponseEntity.noContent().build();
    }
}