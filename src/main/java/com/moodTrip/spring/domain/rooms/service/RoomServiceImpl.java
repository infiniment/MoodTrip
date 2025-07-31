package com.moodTrip.spring.domain.rooms.service;

import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest;
import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest.ScheduleDto.DateRangeDto;
import com.moodTrip.spring.domain.rooms.dto.request.UpdateRoomRequest;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.entity.EmotionRoom;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.EmotionRoomRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.global.common.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    // 방 생성 로직
    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
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
                .isDeleteRoom(false)
                .build();

        Room savedRoom = roomRepository.save(room);

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


}
