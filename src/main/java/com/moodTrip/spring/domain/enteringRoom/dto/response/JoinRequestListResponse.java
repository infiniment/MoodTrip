package com.moodTrip.spring.domain.enteringRoom.dto.response;

import com.moodTrip.spring.domain.enteringRoom.entity.EnteringRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequestListResponse {

    // 방장용으로 신청 목록 조회 dto

    private Long joinRequestId;
    private String applicantNickname;
    private String applicantProfileImage;
    private String message;
    private String appliedAt;            // "2025-07-01 14:31" 형식
    private String timeAgo;              // "2시간 전" 형식
    private String status;               // PENDING, APPROVED, REJECTED
    private String priority;             // HIGH, NORMAL
    private boolean isVerified;          // 신원 인증 여부
    private boolean hasPhoneVerified;    // 연락처 인증 여부

    // 🔥 엔티티에서 DTO로 변환하는 정적 메서드
    public static JoinRequestListResponse from(EnteringRoom entity) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedTime = entity.getCreatedAt().format(formatter);

        return JoinRequestListResponse.builder()
                .joinRequestId(entity.getEnteringRoomId())
                .applicantNickname(entity.getApplicant().getNickname())
                .applicantProfileImage(getProfileImage(entity))
                .message(entity.getMessage())
                .appliedAt(formattedTime)
                .timeAgo(calculateTimeAgo(entity.getCreatedAt()))
                .status(entity.getStatus().toString())
                .priority(calculatePriority(entity))
                .isVerified(false) // 임시값 - 나중에 실제 인증 정보 연결
                .hasPhoneVerified(true) // 임시값 - 나중에 실제 인증 정보 연결
                .build();
    }

    // 프로필 이미지 가져오기
    private static String getProfileImage(EnteringRoom entity) {
        // 나중에 Profile 엔티티와 연결
        return "/image/fix/moodtrip.jpg";
    }

    // 시간 차이 계산 ("2시간 전" 형식)
    private static String calculateTimeAgo(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(createdAt, now).toMinutes();

        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";
        if (minutes < 1440) return (minutes / 60) + "시간 전";
        return (minutes / 1440) + "일 전";
    }

    // 우선순위 계산 (임시 로직)
    private static String calculatePriority(EnteringRoom entity) {
        // 신청 후 2시간 이내면 HIGH, 그 외는 NORMAL
        LocalDateTime now = LocalDateTime.now();
        long hours = java.time.Duration.between(entity.getCreatedAt(), now).toHours();
        return hours <= 2 ? "HIGH" : "NORMAL";
    }
}