package com.moodTrip.spring.domain.rooms.service;


import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest;
import com.moodTrip.spring.domain.rooms.dto.request.UpdateRoomRequest;
import com.moodTrip.spring.domain.rooms.dto.response.RoomMemberResponse;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;

import java.util.List;


public interface RoomService {
    RoomResponse createRoom(RoomRequest request, Long memberPk); // 방 생성
    RoomResponse getRoomById(Long roomId); // 특정 방 조회(상세 페이지)
    List<RoomResponse> getAllRooms(); // 모든 방 조회
//    void addEmotionRooms(Room room, List<EmotionDto> emotions); // 방에서 등록한 감정 저장
    void deleteRoomById(Long roomId); // 방 삭제
    RoomResponse updateRoom(Long roomId, UpdateRoomRequest request); // 방 수정
    void joinRoom(Member member, Room room, String role); // 방 참여
    void leaveRoom(Member member, Room room); // 방 나가기
    boolean isMemberInRoom(Member member, Room room); // 방 참여 여부 확인
    List<RoomMemberResponse> getActiveMembers(Room room); // 방의 활성 멤버 조회
    Room getRoomEntityById(Long roomId);
    RoomResponse toResponseDto(Room room);
}
