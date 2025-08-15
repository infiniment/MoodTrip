package com.moodTrip.spring.domain.enteringRoom.service;

import com.moodTrip.spring.domain.enteringRoom.dto.response.CompanionRoomListResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// 방 입장하기 관련 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanionRoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;

    // 전체 방 목록 조회
    public List<CompanionRoomListResponse> getAllRooms() {

        try {
            List<Room> rooms = roomRepository.findByIsDeleteRoomFalse();

            List<CompanionRoomListResponse> responses = rooms.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return responses;

        } catch (Exception e) {
            throw new RuntimeException("방 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    // 키워드로 방 검색
    public List<CompanionRoomListResponse> searchRooms(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllRooms();
        }

        try {
            List<Room> allRooms = roomRepository.findByIsDeleteRoomFalse();
            String searchKeyword = keyword.toLowerCase().trim();

            List<Room> filteredRooms = allRooms.stream()
                    .filter(room -> matchesKeyword(room, searchKeyword))
                    .collect(Collectors.toList());

            return filteredRooms.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("방 검색 중 오류가 발생했습니다.", e);
        }
    }

    // 지역별 방 필터링 (아직 완성되지 않음) =>
    public List<CompanionRoomListResponse> getRoomsByRegion(String region) {
        if (region == null || region.trim().isEmpty()) {
            return getAllRooms();
        }

        try {
            List<Room> allRooms = roomRepository.findByIsDeleteRoomFalse();

            List<Room> regionRooms = allRooms.stream()
                    .filter(room -> room.getDestinationName() != null &&
                            room.getDestinationName().contains(region))
                    .collect(Collectors.toList());

            return regionRooms.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("지역별 방 조회 중 오류가 발생했습니다.", e);
        }
    }

    // 최대 인원별 방 필터링
    public List<CompanionRoomListResponse> getRoomsByMaxParticipants(String maxParticipantsFilter) {

        if (maxParticipantsFilter == null || maxParticipantsFilter.trim().isEmpty()) {
            return getAllRooms();
        }

        try {
            List<Room> allRooms = roomRepository.findByIsDeleteRoomFalse();

            List<Room> filteredRooms = allRooms.stream()
                    .filter(room -> matchesParticipantFilter(room, maxParticipantsFilter))
                    .collect(Collectors.toList());

            return filteredRooms.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("인원별 방 조회 중 오류가 발생했습니다.", e);
        }
    }

    // 🔥 새로 추가: 조회수 증가 포함 방 상세 조회 메서드
    @Transactional  // 쓰기 작업이므로 @Transactional 필요
    public CompanionRoomListResponse getRoomDetailWithViewCount(Long roomId) {
        try {
            // 방 조회
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));

            // 🔥 조회수 증가
            room.incrementViewCount();
            Room updatedRoom = roomRepository.save(room);

            // 로그 출력 (확인용)
            System.out.println("🔥 조회수 증가! 방ID: " + roomId + ", 현재 조회수: " + updatedRoom.getViewCount());

            // Response 반환 (증가된 조회수 포함)
            return convertToResponseWithActualViewCount(updatedRoom);

        } catch (Exception e) {
            throw new RuntimeException("방 상세 조회 중 오류가 발생했습니다.", e);
        }
    }

    // 🔥 새로 추가: 실제 조회수를 사용하는 변환 메서드
    private CompanionRoomListResponse convertToResponseWithActualViewCount(Room room) {
        try {
            // 실제 참여자 수 계산
            Long actualParticipantCount = roomMemberRepository.countByRoomAndIsActiveTrue(room);

            // 🔥 실제 DB의 조회수 사용
            Integer actualViewCount = room.getViewCount() != null ? room.getViewCount() : 0;

            // 기본 DTO 생성 (실제 조회수로)
            CompanionRoomListResponse response = CompanionRoomListResponse.from(room, actualViewCount);

            // 실제 참여자 수와 조회수로 업데이트
            return CompanionRoomListResponse.builder()
                    .id(response.getId())
                    .title(response.getTitle())
                    .location(response.getLocation())
                    .date(response.getDate())
                    .views(response.getViews())  // "5명이 봄" 형식으로 표시
                    .viewCount(actualViewCount)  // 실제 조회수 숫자
                    .description(response.getDescription())
                    .currentParticipants(actualParticipantCount.intValue())
                    .maxParticipants(response.getMaxParticipants())
                    .createdDate(response.getCreatedDate())
                    .image(response.getImage())
                    .urgent(response.getUrgent())
                    .status(response.getStatus())
                    .build();

        } catch (Exception e) {
            return CompanionRoomListResponse.from(room, 0);
        }
    }

    // 기존 엔티티 => dto 변환 (목록 조회용 - 조회수 증가 안함)
    private CompanionRoomListResponse convertToResponse(Room room) {
        try {
            // 실제 참여자 수 계산
            Long actualParticipantCount = roomMemberRepository.countByRoomAndIsActiveTrue(room);

            // 🔥 실제 조회수 사용 (증가시키지는 않음)
            Integer actualViewCount = room.getViewCount() != null ? room.getViewCount() : 0;

            // 기본 DTO 생성 (실제 조회수로)
            CompanionRoomListResponse response = CompanionRoomListResponse.from(room, actualViewCount);

            // 실제 참여자 수와 정확한 상태로 업데이트
            return CompanionRoomListResponse.builder()
                    .id(response.getId())
                    .title(response.getTitle())
                    .location(response.getLocation())
                    .date(response.getDate())
                    .views(response.getViews())  // 실제 조회수로 "X명이 봄" 표시
                    .viewCount(actualViewCount)  // 실제 조회수
                    .description(response.getDescription())
                    .currentParticipants(actualParticipantCount.intValue())
                    .maxParticipants(response.getMaxParticipants())
                    .createdDate(response.getCreatedDate())
                    .image(response.getImage())
                    .urgent(response.getUrgent())
                    .status(response.getStatus())  // DTO에서 계산한 status 그대로 사용!
                    .build();

        } catch (Exception e) {
            return CompanionRoomListResponse.from(room, 0);
        }
    }

    // 키워드 검색 시 매칭 검사
    private boolean matchesKeyword(Room room, String keyword) {
        boolean titleMatch = room.getRoomName() != null &&
                room.getRoomName().toLowerCase().contains(keyword);

        boolean descriptionMatch = room.getRoomDescription() != null &&
                room.getRoomDescription().toLowerCase().contains(keyword);

        boolean destinationMatch = room.getDestinationName() != null &&
                room.getDestinationName().toLowerCase().contains(keyword);

        return titleMatch || descriptionMatch || destinationMatch;
    }

    // 인원 필터링 검사
    private boolean matchesParticipantFilter(Room room, String filter) {
        int maxCount = room.getRoomMaxCount();

        switch (filter) {
            case "2":
                return maxCount == 2;
            case "4":
                return maxCount == 4;
            case "other":
                return maxCount > 4;
            default:
                return true;
        }
    }
}
