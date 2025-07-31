package com.moodTrip.spring.domain.rooms.controller;

import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest;
import com.moodTrip.spring.domain.rooms.dto.request.UpdateRoomRequest;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companion-rooms")
public class RoomApiController {
    private final RoomService roomService;

    // 방 생성
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@RequestBody final RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.ok(response);
    }

    // 방 단건 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    // 방 전체 조회
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    // 방 수정
    @PatchMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId, @RequestBody UpdateRoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, request));
    }

    // 방 삭제
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }
}
