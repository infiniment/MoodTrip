package com.moodTrip.spring.domain.mypageRoom.service;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.mypageRoom.dto.response.CreatedRoomResponse;
import com.moodTrip.spring.domain.mypageRoom.dto.response.JoinedRoomResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.schedule.entity.Schedule;
import com.moodTrip.spring.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

// 마이페이지 방 관련 서비스 구현체
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageRoomServiceImpl implements MypageRoomService {

    // 기존 Repository들을 주입받아서 사용
    private final RoomMemberRepository roomMemberRepository;
    private final RoomRepository roomRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    public List<JoinedRoomResponse> getMyJoinedRooms(Member member) {
        // 현재 회원이 참여 중인 활성 방들 조회, 수민이가 만든 roomMemberRepository 활용
        List<RoomMember> activeRoomMembers = roomMemberRepository.findByMemberAndIsActiveTrue(member);

        // 삭제되지 않은 방들만 필터링하고 DTO로 변환
        List<JoinedRoomResponse> joinedRooms = activeRoomMembers.stream()
                .filter(roomMember -> {
                    // 삭제되지 않은 방만 포함
                    boolean isNotDeleted = !roomMember.getRoom().getIsDeleteRoom();
                    return isNotDeleted;
                })
                .map(roomMember -> {
                    // 마이페이지 전용 DTO로 변환
                    return JoinedRoomResponse.from(roomMember);
                })
                .sorted((r1, r2) -> r2.getJoinedAt().compareTo(r1.getJoinedAt())) // 최근 참여한 방부터
                .collect(Collectors.toList());

        return joinedRooms;
    }


    @Override
    @Transactional(readOnly = true)
    public List<CreatedRoomResponse> getMyCreatedRooms(Member member) {
        List<Room> createdRooms = roomRepository.findByCreatorAndIsDeleteRoomFalse(member);

        return createdRooms.stream()
                .map(CreatedRoomResponse::from)
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))  // 최신순 정렬
                .collect(Collectors.toList());
    }

    // 방 삭제하기 로직
    @Override
    @Transactional
    public void deleteRoom(Long roomId, Member currentMember) throws AccessDeniedException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방이 존재하지 않습니다."));

        RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(currentMember, room)
                .orElseThrow(() -> new IllegalArgumentException("이 방에 참여하지 않은 사용자입니다."));

        if (!"LEADER".equalsIgnoreCase(roomMember.getRole())) {
            throw new AccessDeniedException("방장만 방을 삭제할 수 있습니다.");
        }

        // 스케줄 먼저 삭제
        List<Schedule> schedules = scheduleRepository.findByRoom(room);
        if (!schedules.isEmpty()) {
            scheduleRepository.deleteAll(schedules);
        }

        room.setIsDeleteRoom(true);
        roomRepository.save(room);
    }

    // 방 나가기 (방 입장하기 기능 구현 시 추가 수정 예정)
    @Override
    @Transactional
    public void leaveRoom(Long roomId, Member currentMember) {
        // 방 존재 확인
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방이 존재하지 않습니다."));

        // 현재 사용자가 해당 방에 참여중인지 조회
        RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(currentMember, room)
                .orElseThrow(() -> new IllegalArgumentException("이 방에 참여하지 않은 사용자입니다."));

        // 방장이 나갈 수는 없게하고, 나갈거면 삭제하기
        if ("LEADER".equalsIgnoreCase(roomMember.getRole())) {
            throw new IllegalStateException("방장은 방을 나갈 수 없습니다. 방 삭제를 사용해주세요.");
        }
        // 나가기 처리
        roomMember.setIsActive(false);
    }


}