package com.moodTrip.spring.domain.rooms.service;


import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest;
import com.moodTrip.spring.domain.rooms.dto.request.UpdateRoomRequest;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;

import java.util.List;


public interface RoomService {
    RoomResponse createRoom(RoomRequest request); // 방 생성
    RoomResponse getRoomById(Long roomId); // 특정 방 조회(상세 페이지)
    List<RoomResponse> getAllRooms(); // 모든 방 조회
//    void addEmotionRooms(Room room, List<EmotionDto> emotions); // 방에서 등록한 감정 저장
    void deleteRoomById(Long roomId); // 방 삭제
    RoomResponse updateRoom(Long roomId, UpdateRoomRequest request); // 방 수정
}
