package com.moodTrip.spring.domain.rooms.service;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest;
import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest.ScheduleDto.DateRangeDto;
import com.moodTrip.spring.domain.rooms.dto.request.UpdateRoomRequest;
import com.moodTrip.spring.domain.rooms.dto.response.RoomMemberResponse;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.entity.EmotionRoom;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.EmotionRoomRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.global.common.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.moodTrip.spring.domain.rooms.dto.request.RoomRequest.*;
import static com.moodTrip.spring.global.common.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final EmotionRoomRepository emotionRoomRepository;
    // private final EmotionRepository emotionRepository; // 추후 활성화
    private final RoomMemberRepository roomMemberRepository;
    private final MemberRepository memberRepository;

    // 방 생성 로직
    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request, Long memberPk) {
        // 방 생성 회원 조회
        Member creator = memberRepository.findByMemberPk(memberPk)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        // 날짜 범위 계산
        List<DateRangeDto> ranges = request.getSchedule().getDateRanges();
        LocalDate travelStartDate = ranges.stream()
                .map(r -> r.getStartDate().toLocalDate())
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new CustomException(INVALID_TRAVEL_DATE));

        LocalDate travelEndDate = ranges.stream()
                .map(r -> r.getEndDate().toLocalDate())
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new CustomException(INVALID_TRAVEL_DATE));

        Room room = Room.builder()
                .roomName(request.getRoomName())
                .roomDescription(request.getRoomDescription())
                .roomMaxCount(request.getMaxParticipants())
                .roomCurrentCount(1)
                .travelStartDate(travelStartDate)
                .travelEndDate(travelEndDate)
                .creator(creator)
                .isDeleteRoom(false)
                .build();

        Room savedRoom = roomRepository.save(room);

        // RoomMember로 리더 등록
        RoomMember leader = RoomMember.builder()
                .member(creator)
                .room(savedRoom)
                .role("LEADER")
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        roomMemberRepository.save(leader);

        return RoomResponse.from(savedRoom);
    }

    // Room 단건 조회 서비스
    @Override
    public RoomResponse getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ROOM_NOT_FOUND));
        return RoomResponse.from(room);
    }

    // 방 목록 조회 서비스
    @Override
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }


    // 방 감정 연관 저장 로직
//    @Override
//    public void addEmotionRooms(Room room, List<EmotionDto> emotions) {
//        for (EmotionDto dto : emotions) {
//            // Emotion emotion = emotionRepository.findById(dto.getId())
//            //     .orElseThrow(() -> new CustomException(EMOTION_NOT_FOUND));
//
//            EmotionRoom emotionRoom = EmotionRoom.builder()
//                    .room(room)
//                    // .emotion(emotion)
//                    .build();
//
//            emotionRoomRepository.save(emotionRoom);
//        }
//    }

    // 방 삭제 (soft delete)
    @Override
    public void deleteRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ROOM_NOT_FOUND));
        room.setIsDeleteRoom(true);
        roomRepository.save(room);
    }

    // 방 수정
    @Override
    public RoomResponse updateRoom(Long roomId, UpdateRoomRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ROOM_NOT_FOUND));

        if(request.getRoomName() != null) {
            room.setRoomName(request.getRoomName());
        }

        if(request.getRoomDescription() != null) {
            room.setRoomDescription(request.getRoomDescription());
        }

        if(request.getMaxParticipants() > 0 && request.getMaxParticipants() >= room.getRoomCurrentCount()) {
            room.setRoomMaxCount(request.getMaxParticipants());
        }else {
            throw new CustomException(INVALID_MAX_PARTICIPANT); // 예외 처리
        }

        Room updated = roomRepository.save(room);
        return RoomResponse.from(updated);
    }

    @Override
    public void joinRoom(Member member, Room room, String role) {
        // 방에 참여 중인지 확인
        if(roomMemberRepository.findByMemberAndRoom(member, room).isPresent()) {
            throw new CustomException(ROOM_MEMBER_ALREADY_EXISTS);
        }

        RoomMember roomMember = RoomMember.builder()
                .member(member)
                .room(room)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        roomMemberRepository.save(roomMember);
    }


    @Override
    public void leaveRoom(Member member, Room room) {
        RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, room)
                .orElseThrow(() -> new CustomException(ROOM_MEMBER_NOT_FOUND));

        roomMember.setIsActive(false);
        roomMemberRepository.save(roomMember);
    }

    @Override
    public boolean isMemberInRoom(Member member, Room room) {
        return roomMemberRepository.findByMemberAndRoom(member, room).isPresent();
    }

    @Override
    public List<RoomMemberResponse> getActiveMembers(Room room) {
        List<RoomMember> roomMembers = roomMemberRepository.findByRoomAndIsActiveTrue(room);

        return roomMembers.stream()
                .map(RoomMemberResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public Room getRoomEntityById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ROOM_NOT_FOUND));
    }

    @Override
    public RoomResponse toResponseDto(Room room) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .roomDescription(room.getRoomDescription())
                .maxParticipants(room.getRoomMaxCount())
                .currentParticipants(room.getRoomCurrentCount())
                .travelStartDate(room.getTravelStartDate().format(formatter))
                .travelEndDate(room.getTravelEndDate().format(formatter))
                .destinationName(room.getDestinationName())
                .destinationLat(room.getDestinationLat())
                .destinationLon(room.getDestinationLon())
                .build();
    }
}
