package com.moodTrip.spring.domain.enteringRoom.dto.response;

import com.moodTrip.spring.domain.rooms.entity.Room;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanionRoomListResponse {

    private Long id;
    private String title;
    private String location;
    private String date;
    private String views;
    private Integer viewCount;
    private String description;
    //private List<String> emotions; // 감정은 나중에 로직 구현 시 수정!!!
    private Integer currentParticipants;
    private Integer maxParticipants;
    private String createdDate;
    private String image;
    private Boolean urgent;
    private String status;

    // Room 엔티티에서 dto로 변환하는 메서드
    public static CompanionRoomListResponse from(Room room) {
        // 기본 조회수는 0
        return from(room, 0);
    }

    // 변환 메서드
    public static CompanionRoomListResponse from(Room room, Integer viewCount) {
        return CompanionRoomListResponse.builder()
                .id(room.getRoomId())
                .title(room.getRoomName())
                .location(room.getDestinationName())
                .date(formatTravelDate(room))
                .views(formatViews(viewCount))
                .viewCount(viewCount)
                .description(room.getRoomDescription())
                //.emotions(java.util.Collections.emptyList())
                .currentParticipants(room.getRoomCurrentCount())
                .maxParticipants(room.getRoomMaxCount())
                .createdDate(formatCreatedDate(room))
                .image("/image/fix/moodtrip.png")
                .urgent(calculateUrgent(room))
                .status(calculateStatus(room))
                .build();
    }

    // 날짜 변경하기 백 => 프론트 형태로
    private static String formatTravelDate(Room room) {
        if (room.getTravelStartDate() == null) {
            return "날짜 미정";
        }
        java.time.LocalDate startDate = room.getTravelStartDate();
        int month = startDate.getMonthValue();
        int dayOfMonth = startDate.getDayOfMonth();
        // 주차 계산 (1~7일=첫째주, 8~14일=둘째주)
        int weekNumber = (dayOfMonth - 1) / 7 + 1;
        String[] weekNames = {"첫째주", "둘째주", "셋째주", "넷째주", "다섯째주"};
        String weekName = weekNumber <= 5 ? weekNames[weekNumber - 1] : "마지막주";

        return month + "월 " + weekName;
    }

    // viewcount를 정수에서 string 형태로 몇명이 봄 형태로 변환
    private static String formatViews(Integer viewCount) {
        if (viewCount == null || viewCount == 0) {
            return "0명이 봄";
        }
        return viewCount + "명이 봄";
    }

    // 생성일 나타내기
    private static String formatCreatedDate(Room room) {
        if (room.getCreatedAt() == null) {
            return "";
        }

        // 생성일을 yy/MM/dd 형식으로 변환
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("yy/MM/dd");

        String startDate = room.getCreatedAt().format(formatter);

        // 여행 종료일이 있으면 함께 표시
        if (room.getTravelEndDate() != null) {
            String endDate = room.getTravelEndDate().format(
                    java.time.format.DateTimeFormatter.ofPattern("yy/MM/dd")
            );
            return startDate + " ~ " + endDate;
        }

        return startDate;
    }

    // 마감 임박 계산하기
    private static Boolean calculateUrgent(Room room) {
        if (room.getTravelStartDate() == null) {
            return false;
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate travelDate = room.getTravelStartDate();

        // 현재부터 여행일까지 남은 일수 계산
        long daysUntilTravel = java.time.temporal.ChronoUnit.DAYS.between(today, travelDate);

        // 7일 이내이고 미래 날짜면 urgent
        return daysUntilTravel >= 0 && daysUntilTravel <= 7;
    }

    // 방 상태 모집중, 모집 완료 나타내기
    private static String calculateStatus(Room room) {
        if (room.getRoomCurrentCount() >= room.getRoomMaxCount()) {
            return "모집완료";
        } else {
            return "모집중";
        }
    }
}