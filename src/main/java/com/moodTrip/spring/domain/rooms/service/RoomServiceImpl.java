package com.moodTrip.spring.domain.rooms.service;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.emotion.entity.Emotion;
import com.moodTrip.spring.domain.emotion.repository.EmotionRepository;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest;
import com.moodTrip.spring.domain.rooms.dto.request.RoomRequest.ScheduleDto.DateRangeDto;
import com.moodTrip.spring.domain.rooms.dto.request.UpdateRoomRequest;
import com.moodTrip.spring.domain.rooms.dto.response.RoomCardDto;
import com.moodTrip.spring.domain.rooms.dto.response.RoomMemberResponse;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.entity.EmotionRoom;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.repository.EmotionRoomRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.global.common.exception.CustomException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.moodTrip.spring.global.common.code.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final EmotionRoomRepository emotionRoomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final MemberRepository memberRepository;
    private final EmotionRepository emotionRepository;
    private final AttractionRepository attractionRepository;

    @Transactional(readOnly = true)
    @Override
    public Room getRoomWithAttraction(Long roomId) {
        return roomRepository.findWithAttractionByRoomId(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found: " + roomId));
    }

    // 방 생성 로직 - 감정 저장 기능 활성화
    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request, Long memberPk) {

        // 방 생성 회원 조회
        Member creator = memberRepository.findByMemberPk(memberPk)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 날짜 범위 계산
        List<DateRangeDto> ranges = request.getSchedule().getDateRanges();
        LocalDate travelStartDate = ranges.stream()
                .map(DateRangeDto::getStartDate)   // 이미 LocalDate
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new CustomException(INVALID_TRAVEL_DATE));

        LocalDate travelEndDate = ranges.stream()
                .map(DateRangeDto::getEndDate)     // 이미 LocalDate
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

        log.info("createRoom: attractionId={}", request.getAttractionId());
        if (request.getAttractionId() == null) {
            throw new CustomException(ATTRACTION_NOT_FOUND);
        }

        // Attraction 필수 연동
        Attraction attr = attractionRepository.findById(request.getAttractionId())
                .orElseGet(() -> attractionRepository.findByContentId(request.getAttractionId())
                        .orElseThrow(() -> new CustomException(ATTRACTION_NOT_FOUND)));
        room.setAttraction(attr);

        // 하위호환용 레거시 필드 동기화 (나중에 필드 제거 전까지 유지)
        room.setDestinationName(attr.getTitle());
        room.setDestinationCategory(attr.getAddr1());

        if (attr.getMapY() != null) room.setDestinationLat(java.math.BigDecimal.valueOf(attr.getMapY())); // 위도
        if (attr.getMapX() != null) room.setDestinationLon(java.math.BigDecimal.valueOf(attr.getMapX())); // 경도

        Room savedRoom = roomRepository.save(room);

        // 🎯 감정 태그 저장 및 연관 처리 (기존 주석 해제하고 수정)
        if (request.getEmotions() != null && !request.getEmotions().isEmpty()) {
            log.info("감정 태그 {} 개 저장 시작", request.getEmotions().size());

            for (RoomRequest.EmotionDto emotionDto : request.getEmotions()) {
                // Long을 Integer로 변환 (Emotion 엔티티의 tagId가 Integer임)
                Integer tagId = emotionDto.getTagId().intValue();

                Emotion emotion = emotionRepository.findById(Long.valueOf(tagId))
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 감정 ID: " + tagId));

                EmotionRoom emotionRoom = EmotionRoom.builder()
                        .room(savedRoom)
                        .emotion(emotion)
                        .build();

                emotionRoomRepository.save(emotionRoom);
                log.debug("감정 저장됨: 방 ID {}, 감정 ID {}, 감정명 {}",
                        savedRoom.getRoomId(), emotion.getTagId(), emotion.getTagName());
            }

            log.info("감정 태그 {} 개 저장 완료", request.getEmotions().size());
        }

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
    @Transactional
    @Override
    public List<RoomResponse> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        // 트랜잭션 내에서 DTO 변환까지 완료
        return rooms.stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }

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
        return RoomResponse.from(room);
    }

    // 방 목록을 RoomCardDto로 변환해 반환
    @Override
    public List<RoomCardDto> getRoomCards() {
        return roomRepository.findAll().stream()
                .map(this::toRoomCardDto)
                .collect(Collectors.toList());
    }

    // Room 엔터티 -> RoomCardDto 변환 메서드 - 감정 태그 조회 기능 추가
    private RoomCardDto toRoomCardDto(Room room) {
        String status = (room.getRoomCurrentCount() >= room.getRoomMaxCount() * 0.5)
                ? "마감임박"
                : "모집중";

        String image = (room.getAttraction() != null && room.getAttraction().getFirstImage() != null)
                ? room.getAttraction().getFirstImage()
                : "/image/creatingRoom/landscape-placeholder-svgrepo-com.svg";

        // 🎯 방의 감정 태그들 조회하기
        List<String> tags = emotionRoomRepository.findByRoom(room).stream()
                .map(emotionRoom -> emotionRoom.getEmotion().getTagName())
                .collect(Collectors.toList());

        return RoomCardDto.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .roomDescription(room.getRoomDescription())
                .destinationCategory(room.getDestinationCategory())
                .destinationName(room.getDestinationName())
                .maxParticipants(room.getRoomMaxCount())
                .currentParticipants(room.getRoomCurrentCount())
                .travelStartDate(room.getTravelStartDate() != null ? room.getTravelStartDate().toString() : null)
                .travelEndDate(room.getTravelEndDate() != null ? room.getTravelEndDate().toString() : null)
                .image(image)
                .tags(tags)  // 🎯 여기에 감정 태그들이 들어감
                .status(status)
                .createDate(room.getCreatedAt() != null ? room.getCreatedAt().toString() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomCardDto> searchRooms(String region, String keyword) {
        return roomRepository.findAll().stream()
                .filter(r -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    String dn = r.getDestinationName();
                    String rn = r.getRoomName();
                    return (dn != null && dn.contains(keyword))
                            || (rn != null && rn.contains(keyword));
                })
                .map(this::toRoomCardDto)
                .collect(Collectors.toList());
    }
}