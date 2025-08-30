package com.moodTrip.spring.domain.mainpage.dto.response;

import com.moodTrip.spring.domain.rooms.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainPageRoomResponse {

    private Long roomId;
    private String roomName;           // 방 제목
    private String attractionName;     // 관광지 이름
    private String location;           // 지역
    private String travelDate;         // "7월 셋째주" 형식
    private Integer currentParticipants;
    private Integer maxParticipants;
    private String status;             // "모집중", "모집완료", "마감임박"
    private Integer viewCount;         // 조회수
    private String roomImage;          // 방 대표 이미지
    private String creatorNickname;    // 방장 닉네임
    private String participantsInfo;   // "3 / 4" 형식
    private String periodInfo;         // "25/11/21 - 25/11/27" 형식

    public static MainPageRoomResponse from(Room room) {
        return MainPageRoomResponse.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .attractionName(getAttractionName(room))
                .location(getLocation(room))
                .travelDate(formatTravelDate(room))
                .currentParticipants(room.getRoomCurrentCount())
                .maxParticipants(room.getRoomMaxCount())
                .status(calculateStatus(room))
                .viewCount(room.getViewCount())
                .roomImage(getRoomImage(room)) // 수정된 부분
                .creatorNickname(room.getCreator() != null ? room.getCreator().getNickname() : "익명")
                .participantsInfo(room.getRoomCurrentCount() + " / " + room.getRoomMaxCount())
                .periodInfo(formatPeriodInfo(room))
                .build();
    }

    // 방 이미지 가져오기

    private static String getRoomImage(Room room) {
        // Attraction에서 이미지 가져오기
        if (room.getAttraction() != null && room.getAttraction().getFirstImage() != null
                && !room.getAttraction().getFirstImage().trim().isEmpty()) {
            return room.getAttraction().getFirstImage();
        }

        // Attraction 이미지가 없으면 기본 이미지들 중 선택
        String[] defaultImages = {
                "/image/fix/moodtrip.png"
        };

        // roomId를 기반으로 이미지 선택 (일관성 유지)
        int imageIndex = (int) (room.getRoomId() % defaultImages.length);
        return defaultImages[imageIndex];
    }

    // attraction에서 관광지 이름 가져오기
    private static String getAttractionName(Room room) {
        if (room.getAttraction() != null) {
            return room.getAttraction().getTitle(); // getAttractionName() → getTitle()로 변경
        }
        // 하위 호환성: Attraction이 없는 기존 데이터 처리
        return room.getDestinationName() != null ? room.getDestinationName() : "목적지 미정";
    }

    // 지역 정보 추출
    private static String getLocation(Room room) {
        if (room.getAttraction() != null && room.getAttraction().getAreaCode() != null) {
            return convertAreaCodeToRegionName(room.getAttraction().getAreaCode());
        }
        // Attraction이 없는 경우 기본값
        return "전국";
    }

    // 지역 코드를 지역명으로 변환
    private static String convertAreaCodeToRegionName(Integer areaCode) {
        switch (areaCode) {
            case 1: return "서울";
            case 2: return "인천";
            case 3: return "대전";
            case 4: return "대구";
            case 5: return "광주";
            case 6: return "부산";
            case 7: return "울산";
            case 8: return "세종";
            case 31: return "경기";
            case 32: return "강원";
            case 33: return "충북";
            case 34: return "충남";
            case 35: return "경북";
            case 36: return "경남";
            case 37: return "전북";
            case 38: return "전남";
            case 39: return "제주";
            default: return "전국";
        }
    }

    // 여행 날짜 형식 변경
    private static String formatTravelDate(Room room) {
        if (room.getTravelStartDate() == null) {
            return "날짜 미정";
        }

        java.time.LocalDate startDate = room.getTravelStartDate();
        int month = startDate.getMonthValue();
        int dayOfMonth = startDate.getDayOfMonth();

        // 주차 계산 (1~7일=첫째주, 8~14일=둘째주, ...)
        int weekNumber = (dayOfMonth - 1) / 7 + 1;
        String[] weekNames = {"첫째주", "둘째주", "셋째주", "넷째주", "다섯째주"};
        String weekName = weekNumber <= 5 ? weekNames[weekNumber - 1] : "마지막주";

        return month + "월 " + weekName;
    }

    // 기간 정보 변환
    private static String formatPeriodInfo(Room room) {
        if (room.getTravelStartDate() == null) {
            return "날짜 미정";
        }

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yy/MM/dd");
        String startDate = room.getTravelStartDate().format(formatter);

        if (room.getTravelEndDate() != null) {
            String endDate = room.getTravelEndDate().format(formatter);
            return startDate + " - " + endDate;
        }

        return startDate;
    }

    // 방 상태 계산 (모집중, 모집완료, 마감임박)
    private static String calculateStatus(Room room) {
        // 1순위: 정원이 가득 찬 경우
        if (room.getRoomCurrentCount() >= room.getRoomMaxCount()) {
            return "모집완료";
        }

        // 2순위: 마감 임박 체크 (여행 시작일이 7일 이내)
        if (room.getTravelStartDate() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate travelDate = room.getTravelStartDate();
            long daysUntilTravel = java.time.temporal.ChronoUnit.DAYS.between(today, travelDate);

            if (daysUntilTravel >= 0 && daysUntilTravel <= 7) {
                return "마감임박";
            }
        }

        // 3순위: 기본값
        return "모집중";
    }
}