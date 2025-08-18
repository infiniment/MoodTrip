package com.moodTrip.spring.domain.enteringRoom.service;

import com.moodTrip.spring.domain.enteringRoom.dto.response.CompanionRoomListResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    // 지역별 방 필터링 - destination_category 사용
    public List<CompanionRoomListResponse> getRoomsByRegion(String region) {
        if (region == null || region.trim().isEmpty()) {
            return getAllRooms();
        }

        try {
            List<Room> allRooms = roomRepository.findByIsDeleteRoomFalse();

            List<Room> regionRooms = allRooms.stream()
                    .filter(room -> matchesRegion(room, region))
                    .collect(Collectors.toList());

            return regionRooms.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("지역별 방 조회 중 오류가 발생했습니다.", e);
        }
    }

    // 정확한 지역 매칭 메서드
    private boolean matchesRegion(Room room, String region) {
        if (room.getDestinationCategory() == null) {
            return false;
        }

        String category = room.getDestinationCategory();

        // 지역명 매핑 (정확한 매칭)
        switch (region) {
            case "서울":
                return category.contains("서울특별시") || category.contains("서울");
            case "인천":
                return category.contains("인천광역시") || category.contains("인천");
            case "경기":
                return category.contains("경기도") || category.contains("경기");
            case "부산":
                return category.contains("부산광역시") || category.contains("부산");
            case "대구":
                return category.contains("대구광역시") || category.contains("대구");
            case "광주":
                return category.contains("광주광역시") || category.contains("광주");
            case "대전":
                return category.contains("대전광역시") || category.contains("대전");
            case "울산":
                return category.contains("울산광역시") || category.contains("울산");
            case "강원":
                return category.contains("강원도") || category.contains("강원특별자치도") || category.contains("강원");
            case "충북":
                return category.contains("충청북도") || category.contains("충북");
            case "충남":
                return category.contains("충청남도") || category.contains("충남");
            case "전북":
                return category.contains("전라북도") || category.contains("전북특별자치도") || category.contains("전북");
            case "전남":
                return category.contains("전라남도") || category.contains("전남");
            case "경북":
                return category.contains("경상북도") || category.contains("경북");
            case "경남":
                return category.contains("경상남도") || category.contains("경남");
            case "제주":
                return category.contains("제주특별자치도") || category.contains("제주");
            default:
                // 기본: 단순 포함 검색
                return category.contains(region);
        }
    }

    // CompanionRoomService.java에 추가

    // 정렬된 방 목록 조회
    public List<CompanionRoomListResponse> getAllRoomsSorted(String sortType) {
        try {
            List<Room> rooms = roomRepository.findByIsDeleteRoomFalse();

            // 정렬 적용
            List<Room> sortedRooms = applySorting(rooms, sortType);

            return sortedRooms.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("정렬된 방 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    private List<Room> applySorting(List<Room> rooms, String sortType) {
        if ("popular".equals(sortType)) {
            // 인기순 (조회수 높은 순)
            return rooms.stream()
                    .sorted((r1, r2) -> {
                        Integer views1 = r1.getViewCount() != null ? r1.getViewCount() : 0;
                        Integer views2 = r2.getViewCount() != null ? r2.getViewCount() : 0;
                        return views2.compareTo(views1);
                    })
                    .collect(Collectors.toList());
        }

        // 기본 정렬 (default든 null이든 상관없이) - 원본 순서 그대로
        return rooms;
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

    // 조회수 증가 포함 방 상세 조회 메서드
    @Transactional  // 쓰기 작업이므로 @Transactional 필요
    public CompanionRoomListResponse getRoomDetailWithViewCount(Long roomId) {
        try {
            // 방 조회
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));

            // 조회수 증가
            room.incrementViewCount();
            Room updatedRoom = roomRepository.save(room);

            // 로그 출력 (확인용)
            System.out.println("조회수 증가! 방ID: " + roomId + ", 현재 조회수: " + updatedRoom.getViewCount());

            // Response 반환 (증가된 조회수 포함)
            return convertToResponseWithActualViewCount(updatedRoom);

        } catch (Exception e) {
            throw new RuntimeException("방 상세 조회 중 오류가 발생했습니다.", e);
        }
    }

    // 실제 조회수를 사용하는 변환 메서드
    private CompanionRoomListResponse convertToResponseWithActualViewCount(Room room) {
        try {
            // 실제 참여자 수 계산
            Long actualParticipantCount = roomMemberRepository.countByRoomAndIsActiveTrue(room);

            // 실제 DB의 조회수 사용
            Integer actualViewCount = room.getViewCount() != null ? room.getViewCount() : 0;

            // 기본 DTO 생성 (실제 조회수로)
            CompanionRoomListResponse response = CompanionRoomListResponse.from(room, actualViewCount);

            // 실제 참여자 수와 조회수로 업데이트
            return CompanionRoomListResponse.builder()
                    .id(response.getId())
                    .title(response.getTitle())
                    .location(response.getLocation())
                    .category(room.getDestinationCategory())
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

            // 실제 조회수 사용 (증가시키지는 않음)
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

    // 키워드 검색시 매칭 검사
    private boolean matchesKeyword(Room room, String keyword) {
        // 방 제목 매칭
        boolean titleMatch = room.getRoomName() != null &&
                room.getRoomName().toLowerCase().contains(keyword);

        // 방 설명 매칭
        boolean descriptionMatch = room.getRoomDescription() != null &&
                room.getRoomDescription().toLowerCase().contains(keyword);

        // destination_name 매칭
        boolean destinationNameMatch = room.getDestinationName() != null &&
                room.getDestinationName().toLowerCase().contains(keyword);

        // destination_category 매칭
        boolean destinationCategoryMatch = room.getDestinationCategory() != null &&
                room.getDestinationCategory().toLowerCase().contains(keyword);

        // 하나라도 매칭되면 결과에 포함
        return titleMatch || descriptionMatch || destinationNameMatch || destinationCategoryMatch;
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
