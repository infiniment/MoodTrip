// 📁 src/main/java/com/moodTrip/spring/domain/mypageRoom/service/MypageRoomService.java

package com.moodTrip.spring.domain.mypageRoom.service;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.mypageRoom.dto.response.CreatedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.dto.response.JoinedRoomResponse;

import java.util.List;

//마이페이지 방 관련 서비스 인터페이스
// 내가 입장한 방 목록 조회
// 내가 만든 방 목록 조회 (추후 구현)
// 방 나가기, 방 삭제 등 (추후 구현)

public interface MypageRoomService {

    // 현재 로그인한 사용자가 참여 중인 방 목록 조회
    List<JoinedRoomResponse> getMyJoinedRooms(Member member);

    // 현재 로그인한 사용자가 생성한 방 목록 조회
    List<CreatedRoomResponse> getMyCreatedRooms(Member member);

}