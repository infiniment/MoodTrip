package com.moodTrip.spring.domain.enteringRoom.service;

import com.moodTrip.spring.domain.enteringRoom.dto.response.CompanionRoomListResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// 방 입장하기 관련 서비스
@Slf4j
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

    // 키워드 검색
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

    // 지역별 방 필터링
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
    public CompanionRoomListResponse getRoomDetailWithViewCount(Long roomId) {
        try {
            // 방 조회
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));

            // 조회수 증가
            room.incrementViewCount();
            Room updatedRoom = roomRepository.save(room);

            log.info("조회수 증가! 방ID: {}, 현재 조회수: {}", roomId, updatedRoom.getViewCount());

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

            // 🔥 감정 태그 추출 추가!
            List<String> emotions = extractEmotions(room);
            log.info("방 ID: {}, 감정 개수: {}, 감정 목록: {}", room.getRoomId(), emotions.size(), emotions);

            // 기본 DTO 생성 (실제 조회수로)
            CompanionRoomListResponse response = CompanionRoomListResponse.from(room, actualViewCount);

            // 실제 참여자 수와 조회수로 업데이트
            return CompanionRoomListResponse.builder()
                    .id(response.getId())
                    .title(response.getTitle())
                    .location(response.getLocation())
                    .category(room.getDestinationCategory())
                    .date(response.getDate())
                    .views(response.getViews())
                    .viewCount(actualViewCount)
                    .description(response.getDescription())
                    .emotions(emotions)  // 🔥 감정 데이터 추가!
                    .currentParticipants(actualParticipantCount.intValue())
                    .maxParticipants(response.getMaxParticipants())
                    .createdDate(response.getCreatedDate())
                    .image((room.getAttraction() != null && room.getAttraction().getFirstImage() != null)
                            ? room.getAttraction().getFirstImage()
                            : "/static/image/default.png")
                    .urgent(response.getUrgent())
                    .status(response.getStatus())
                    .build();

        } catch (Exception e) {
            log.error("convertToResponseWithActualViewCount 오류: ", e);
            return CompanionRoomListResponse.from(room, 0);
        }
    }

    // 기존 엔티티 => dto 변환 (목록 조회용 - 조회수 증가 안함) (수정됨)
    private CompanionRoomListResponse convertToResponse(Room room) {
        try {
            // 실제 참여자 수 계산
            Long actualParticipantCount = roomMemberRepository.countByRoomAndIsActiveTrue(room);

            // 실제 조회수 사용 (증가시키지는 않음)
            Integer actualViewCount = room.getViewCount() != null ? room.getViewCount() : 0;

            // 🔥 감정 태그 추출 추가!
            List<String> emotions = extractEmotions(room);

            // 기본 DTO 생성 (실제 조회수로)
            CompanionRoomListResponse response = CompanionRoomListResponse.from(room, actualViewCount);

            // 실제 참여자 수와 정확한 상태로 업데이트
            return CompanionRoomListResponse.builder()
                    .id(response.getId())
                    .title(response.getTitle())
                    .location(response.getLocation())
                    .date(response.getDate())
                    .views(response.getViews())
                    .viewCount(actualViewCount)
                    .description(response.getDescription())
                    .emotions(emotions)  // 🔥 감정 데이터 추가!
                    .currentParticipants(actualParticipantCount.intValue())
                    .maxParticipants(response.getMaxParticipants())
                    .createdDate(response.getCreatedDate())
                    .image(response.getImage())
                    .urgent(response.getUrgent())
                    .status(response.getStatus())
                    .build();

        } catch (Exception e) {
            log.error("convertToResponse 오류: ", e);
            return CompanionRoomListResponse.from(room, 0);
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

    // 감정 추출 메소드
    private List<String> extractEmotions(Room room) {
        try {
            log.debug("감정 추출 시작 - Room ID: {}", room.getRoomId());

            // EmotionRooms 강제 로딩
            if (room.getEmotionRooms() != null) {
                room.getEmotionRooms().size(); // 지연 로딩 강제 실행
                log.debug("EmotionRooms 개수: {}", room.getEmotionRooms().size());
            }

            if (room.getEmotionRooms() == null || room.getEmotionRooms().isEmpty()) {
                log.warn("EmotionRooms가 비어있음 - Room ID: {}", room.getRoomId());
                return java.util.Collections.emptyList();
            }

            List<String> emotions = room.getEmotionRooms().stream()
                    .filter(emotionRoom -> emotionRoom.getEmotion() != null)
                    .map(emotionRoom -> emotionRoom.getEmotion().getTagName())
                    .collect(java.util.stream.Collectors.toList());

            log.debug("추출된 감정들 - Room ID: {}, 감정: {}", room.getRoomId(), emotions);
            return emotions;

        } catch (Exception e) {
            log.error("감정 추출 중 오류 - Room ID: {}, 오류: {}", room.getRoomId(), e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
}