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
import com.moodTrip.spring.domain.rooms.dto.response.RoomListItemResponse;
import com.moodTrip.spring.domain.rooms.dto.response.RoomMemberResponse;
import com.moodTrip.spring.domain.rooms.dto.response.RoomPageResponse;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.entity.EmotionRoom;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.entity.RoomMember;
import com.moodTrip.spring.domain.rooms.enums.RoomErrorCode;
import com.moodTrip.spring.domain.rooms.exception.RoomException;
import com.moodTrip.spring.domain.rooms.repository.EmotionRoomRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.math.BigDecimal.valueOf;


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
                .orElseThrow(() -> new RoomException(
                        RoomErrorCode.ROOM_NOT_FOUND,
                        "Room not found: " + roomId));
    }

    // 방 생성 로직 - 감정 저장 기능 활성화
    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request, Long memberPk) {

        // 방 생성 회원 조회
        Member creator = memberRepository.findByMemberPk(memberPk)
                .orElseThrow(() -> new RoomException(
                        RoomErrorCode.USER_NOT_FOUND,
                        "memberId = " + memberPk));

        // 날짜 범위 계산
        List<DateRangeDto> ranges = request.getSchedule().getDateRanges();
        List<LocalDate> startDates = ranges.stream()
                .map(DateRangeDto::getStartDate)
                .filter(Objects::nonNull)
                .toList();

        LocalDate selectedStartDate = startDates.stream()
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new RoomException(
                        RoomErrorCode.INVALID_DATE,
                        "여행 일자가 유효하지 않습니다. 입력 startDate 목록 = " + startDates
                ));

        log.info("선택된 startDate={}", selectedStartDate);

        List<LocalDate> endDates = ranges.stream()
                .map(DateRangeDto::getEndDate)
                .filter(Objects::nonNull)
                .toList();

        LocalDate selectedEndDate = endDates.stream()
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new RoomException(
                        RoomErrorCode.INVALID_DATE,
                        "여행 일자가 유효하지 않습니다. 입력 endDate 목록 = " + endDates
                ));

        Room room = Room.builder()
                .roomName(request.getRoomName())
                .roomDescription(request.getRoomDescription())
                .roomMaxCount(request.getMaxParticipants())
                .roomCurrentCount(1)
                .travelStartDate(selectedStartDate)
                .travelEndDate(selectedEndDate)
                .creator(creator)
                .isDeleteRoom(false)
                .build();

        log.info("createRoom: attractionId={}", request.getAttractionId());


        if (request.getAttractionId() == null) {
            throw new RoomException(RoomErrorCode.ATTRACTION_NOT_FOUND, "attractionId가 null입니다.");
        }

        // Attraction 필수 연동
        Long attractionId = request.getAttractionId();
        Attraction attr = attractionRepository.findById(attractionId)
                .orElseGet(() -> attractionRepository.findByContentId(attractionId)
                        .orElseThrow(() -> new RoomException(
                                RoomErrorCode.ATTRACTION_NOT_FOUND,
                                "어트랙션을 찾을 수 없습니다. attractionId = " + attractionId
                        )));
        room.setAttraction(attr);

        // 하위호환용 레거시 필드 동기화 (나중에 필드 제거 전까지 유지)
        room.setDestinationName(attr.getTitle());
        room.setDestinationCategory(attr.getAddr1());

        if (attr.getMapY() != null) room.setDestinationLat(valueOf(attr.getMapY())); // 위도
        if (attr.getMapX() != null) room.setDestinationLon(valueOf(attr.getMapX())); // 경도

        Room savedRoom = roomRepository.save(room);

        if (request.getEmotions() != null && !request.getEmotions().isEmpty()) {
            log.info("감정 태그 {} 개 저장 시작", request.getEmotions().size());

            List<Long> emotionIds = request.getEmotions().stream()
                    .map(RoomRequest.EmotionDto::getTagId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            List<Emotion> emotions = emotionRepository.findAllById(emotionIds);
            Set<Long> foundEmotionIds = emotions.stream()
                    .map(Emotion::getTagId)
                    .map(Long::valueOf)
                    .collect(Collectors.toSet());

            List<Long> invalidEmotionIds = emotionIds.stream()
                    .filter(tagId -> !foundEmotionIds.contains(tagId))
                    .collect(Collectors.toList());

            if (!invalidEmotionIds.isEmpty()) {
                throw new RoomException(
                        RoomErrorCode.INVALID_EMOTION,
                        "유효하지 않은 감정 ID 목록 = " + invalidEmotionIds
                );
            }

            List<EmotionRoom> emotionRooms = emotions.stream()
                    .map(emotion -> {
                        EmotionRoom emotionRoom = EmotionRoom.builder().build();
                        emotionRoom.setRoom(savedRoom);
                        emotionRoom.setEmotion(emotion);
                        return emotionRoom;
                    })
                    .collect(Collectors.toList());

            emotionRoomRepository.saveAll(emotionRooms);

            log.info("감정 태그 {} 개 저장 완료", request.getEmotions().size());
        }

        // RoomMember로 리더 등록
        RoomMember leader = RoomMember.builder()
                .role("LEADER")
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        leader.setMember(creator);
        leader.setRoom(savedRoom);

        roomMemberRepository.save(leader);

        return RoomResponse.from(savedRoom);
    }

    // Room 단건 조회 서비스
    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));
        return RoomResponse.from(room);
    }

    // 방 목록 조회 서비스
    @Override
    @Transactional(readOnly = true)
    public RoomPageResponse getAllRooms(Pageable pageable) {
        Page<RoomListItemResponse> page = roomRepository.findAll(pageable)
                .map(RoomListItemResponse::from);
        return RoomPageResponse.from(page);
    }

    private void assertRoomCreator(Room room, Long memberPk) {
        if (memberPk == null
                || room.getCreator() == null
                || room.getCreator().getMemberPk() == null
                || !room.getCreator().getMemberPk().equals(memberPk)) {
            throw new RoomException(RoomErrorCode.ROOM_FORBIDDEN);
        }
    }

    // 방 삭제 (soft delete)
    @Override
    public void deleteRoomById(Long roomId, Long memberPk) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));
        assertRoomCreator(room, memberPk);
        room.setIsDeleteRoom(true);
        roomRepository.save(room);
    }

    // 방 수정
    @Override
    public RoomResponse updateRoom(Long roomId, UpdateRoomRequest request, Long memberPk) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));
        assertRoomCreator(room, memberPk);

        if(request.getRoomName() != null) {
            room.setRoomName(request.getRoomName());
        }

        if(request.getRoomDescription() != null) {
            room.setRoomDescription(request.getRoomDescription());
        }

        if(request.getMaxParticipants() > 0 && request.getMaxParticipants() >= room.getRoomCurrentCount()) {
            room.setRoomMaxCount(request.getMaxParticipants());
        }else {
            throw new RoomException(RoomErrorCode.INVALID_MAX_PARTICIPANT); // 예외 처리
        }

        Room updated = roomRepository.save(room);
        return RoomResponse.from(updated);
    }

    @Override
    public void joinRoom(Member member, Room room, String role) {
        // 방에 참여 중인지 확인
        if(roomMemberRepository.findByMemberAndRoom(member, room).isPresent()) {
            throw new RoomException(RoomErrorCode.ROOM_MEMBER_ALREADY_EXISTS);
        }

        RoomMember roomMember = RoomMember.builder()
                .role(role)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();
        roomMember.setMember(member);
        roomMember.setRoom(room);

        roomMemberRepository.save(roomMember);
    }

    @Override
    public void leaveRoom(Member member, Room room) {
        RoomMember roomMember = roomMemberRepository.findByMemberAndRoom(member, room)
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_MEMBER_NOT_FOUND));

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
                .orElseThrow(() -> new RoomException(RoomErrorCode.ROOM_NOT_FOUND));
    }

    @Override
    public RoomResponse toResponseDto(Room room) {
        return RoomResponse.from(room);
    }

    // 방 목록을 RoomCardDto로 변환해 반환
    @Override
    public List<RoomCardDto> getRoomCards() {
        List<Room> rooms = roomRepository.findAll();
        Map<Long, List<String>> tagsByRoomId = getTagsByRoomId(rooms);

        return rooms.stream()
                .map(room -> toRoomCardDto(room, tagsByRoomId.getOrDefault(room.getRoomId(), List.of())))
                .collect(Collectors.toList());
    }

    private Map<Long, List<String>> getTagsByRoomId(List<Room> rooms) {
        if (rooms.isEmpty()) {
            return Collections.emptyMap();
        }

        return emotionRoomRepository.findByRoomIn(rooms).stream()
                .collect(Collectors.groupingBy(
                        emotionRoom -> emotionRoom.getRoom().getRoomId(),
                        Collectors.mapping(emotionRoom -> emotionRoom.getEmotion().getTagName(), Collectors.toList())
                ));
    }

    // Room 엔터티 -> RoomCardDto 변환 메서드
    private RoomCardDto toRoomCardDto(Room room, List<String> tags) {
        String status = (room.getRoomCurrentCount() >= room.getRoomMaxCount() * 0.5)
                ? "마감임박"
                : "모집중";

        String image = (room.getAttraction() != null && room.getAttraction().getFirstImage() != null)
                ? room.getAttraction().getFirstImage()
                : "/image/creatingRoom/landscape-placeholder-svgrepo-com.svg";

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
                .tags(tags)
                .status(status)
                .createDate(room.getCreatedAt() != null ? room.getCreatedAt().toString() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomCardDto> searchRooms(String region, String keyword) {
        List<Room> rooms = roomRepository.searchByKeyword(keyword);

        Map<Long, List<String>> tagsByRoomId = getTagsByRoomId(rooms);

        return rooms.stream()
                .map(room -> toRoomCardDto(room, tagsByRoomId.getOrDefault(room.getRoomId(), List.of())))
                .collect(Collectors.toList());
    }
}
