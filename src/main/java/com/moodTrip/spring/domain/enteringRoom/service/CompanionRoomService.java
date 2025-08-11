//package com.moodTrip.spring.domain.enteringRoom.service;
//
//import com.moodTrip.spring.domain.enteringRoom.dto.response.CompanionRoomListResponse;
//import com.moodTrip.spring.domain.rooms.entity.Room;
//import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
//import com.moodTrip.spring.domain.rooms.repository.RoomMemberRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * 🏠 동행자 방 관련 서비스
// *
// * 이 서비스는 방 목록 조회 관련 비즈니스 로직을 담당합니다.
// *
// * 주요 책임:
// * 1. Room 엔티티 조회
// * 2. 실제 참여자 수 계산 (RoomMemberRepository 활용)
// * 3. DTO로 변환
// * 4. 필터링, 검색 로직
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class CompanionRoomService {
//
//    private final RoomRepository roomRepository;
//    private final RoomMemberRepository roomMemberRepository;
//
//    // 전체 방 목록 조회
//    public List<CompanionRoomListResponse> getAllRooms() {
//        log.info("🔍 전체 방 목록 조회 시작");
//
//        try {
//            List<Room> rooms = roomRepository.findByIsDeleteRoomFalse();
//            log.info("✅ {}개의 방을 조회했습니다", rooms.size());
//
//            List<CompanionRoomListResponse> responses = rooms.stream()
//                    .map(this::convertToResponse)
//                    .collect(Collectors.toList());
//
//            log.info("🎉 방 목록 조회 완료 - {}개 방 반환", responses.size());
//            return responses;
//
//        } catch (Exception e) {
//            log.error("❌ 방 목록 조회 중 오류: {}", e.getMessage(), e);
//            throw new RuntimeException("방 목록 조회 중 오류가 발생했습니다.", e);
//        }
//    }
//
//    /**
//     * 🔍 키워드로 방 검색
//     */
//    public List<CompanionRoomListResponse> searchRooms(String keyword) {
//        log.info("🔍 방 검색 시작 - 키워드: {}", keyword);
//
//        if (keyword == null || keyword.trim().isEmpty()) {
//            return getAllRooms();
//        }
//
//        try {
//            List<Room> allRooms = roomRepository.findByIsDeleteRoomFalse();
//            String searchKeyword = keyword.toLowerCase().trim();
//
//            List<Room> filteredRooms = allRooms.stream()
//                    .filter(room -> matchesKeyword(room, searchKeyword))
//                    .collect(Collectors.toList());
//
//            log.info("🔍 검색 완료 - '{}'키워드로 {}개 방 발견", keyword, filteredRooms.size());
//
//            return filteredRooms.stream()
//                    .map(this::convertToResponse)
//                    .collect(Collectors.toList());
//
//        } catch (Exception e) {
//            log.error("❌ 방 검색 중 오류: {}", e.getMessage(), e);
//            throw new RuntimeException("방 검색 중 오류가 발생했습니다.", e);
//        }
//    }
//
//    /**
//     * 🔍 지역별 방 필터링
//     */
//    public List<CompanionRoomListResponse> getRoomsByRegion(String region) {
//        log.info("🔍 지역별 방 조회 - 지역: {}", region);
//
//        if (region == null || region.trim().isEmpty()) {
//            return getAllRooms();
//        }
//
//        try {
//            List<Room> allRooms = roomRepository.findByIsDeleteRoomFalse();
//
//            List<Room> regionRooms = allRooms.stream()
//                    .filter(room -> room.getDestinationName() != null &&
//                            room.getDestinationName().contains(region))
//                    .collect(Collectors.toList());
//
//            log.info("🔍 지역별 조회 완료 - '{}' 지역: {}개 방", region, regionRooms.size());
//
//            return regionRooms.stream()
//                    .map(this::convertToResponse)
//                    .collect(Collectors.toList());
//
//        } catch (Exception e) {
//            log.error("❌ 지역별 방 조회 중 오류: {}", e.getMessage(), e);
//            throw new RuntimeException("지역별 방 조회 중 오류가 발생했습니다.", e);
//        }
//    }
//
//    /**
//     * 🔍 최대 인원별 방 필터링
//     */
//    public List<CompanionRoomListResponse> getRoomsByMaxParticipants(String maxParticipantsFilter) {
//        log.info("🔍 인원별 방 조회 - 필터: {}", maxParticipantsFilter);
//
//        if (maxParticipantsFilter == null || maxParticipantsFilter.trim().isEmpty()) {
//            return getAllRooms();
//        }
//
//        try {
//            List<Room> allRooms = roomRepository.findByIsDeleteRoomFalse();
//
//            List<Room> filteredRooms = allRooms.stream()
//                    .filter(room -> matchesParticipantFilter(room, maxParticipantsFilter))
//                    .collect(Collectors.toList());
//
//            log.info("🔍 인원별 조회 완료 - '{}' 필터: {}개 방", maxParticipantsFilter, filteredRooms.size());
//
//            return filteredRooms.stream()
//                    .map(this::convertToResponse)
//                    .collect(Collectors.toList());
//
//        } catch (Exception e) {
//            log.error("❌ 인원별 방 조회 중 오류: {}", e.getMessage(), e);
//            throw new RuntimeException("인원별 방 조회 중 오류가 발생했습니다.", e);
//        }
//    }
//
//    /**
//     * 🏗️ Room → DTO 변환 (실제 참여자 수 포함)
//     */
//    private CompanionRoomListResponse convertToResponse(Room room) {
//        try {
//            // 실제 참여자 수 계산
//            Long actualParticipantCount = roomMemberRepository.countByRoom(room);
//
//            // 기본 DTO 생성
//            CompanionRoomListResponse response = CompanionRoomListResponse.from(room, 0);
//
//            // 실제 참여자 수와 정확한 상태로 업데이트
//            return CompanionRoomListResponse.builder()
//                    .id(response.getId())
//                    .title(response.getTitle())
//                    .location(response.getLocation())
//                    .date(response.getDate())
//                    .views(response.getViews())
//                    .viewCount(response.getViewCount())
//                    .description(response.getDescription())
//                    //.emotions(respsonse.getemotion())
//                    .currentParticipants(actualParticipantCount.intValue()) // 실제 참여자 수
//                    .maxParticipants(response.getMaxParticipants())
//                    .createdDate(response.getCreatedDate())
//                    .image(response.getImage())
//                    .urgent(response.getUrgent())
//                    .status(calculateStatus(actualParticipantCount.intValue(), room.getRoomMaxCount())) // 정확한 상태
//                    .build();
//
//        } catch (Exception e) {
//            log.warn("⚠️ Room 변환 중 오류 - roomId: {}", room.getRoomId());
//            return CompanionRoomListResponse.from(room, 0);
//        }
//    }
//
//    /**
//     * 🔍 키워드 매칭 검사
//     */
//    private boolean matchesKeyword(Room room, String keyword) {
//        boolean titleMatch = room.getRoomName() != null &&
//                room.getRoomName().toLowerCase().contains(keyword);
//
//        boolean descriptionMatch = room.getRoomDescription() != null &&
//                room.getRoomDescription().toLowerCase().contains(keyword);
//
//        boolean destinationMatch = room.getDestinationName() != null &&
//                room.getDestinationName().toLowerCase().contains(keyword);
//
//        return titleMatch || descriptionMatch || destinationMatch;
//    }
//
//    /**
//     * 🔍 인원 필터 매칭 검사
//     */
//    private boolean matchesParticipantFilter(Room room, String filter) {
//        int maxCount = room.getRoomMaxCount();
//
//        switch (filter) {
//            case "2":
//                return maxCount == 2;
//            case "4":
//                return maxCount == 4;
//            case "other":
//                return maxCount > 4;
//            default:
//                return true;
//        }
//    }
//
//    /**
//     * 📊 방 상태 계산
//     */
//    private String calculateStatus(int currentCount, int maxCount) {
//        return currentCount >= maxCount ? "모집완료" : "모집중";
//    }
//}
