package com.moodTrip.spring.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDto {
    private Long reportId;
    private String type; // "ROOM" or "MEMBER"
    private String typeDisplay; // "매칭방" or "회원"
    private String targetSummary; // 신고 대상 요약 (방 제목 또는 회원 닉네임)
    private String reporterNickname; // 신고한 사람
    private String reportedNickname; // 신고당한 사람 (방 신고의 경우 방장 닉네임)
    private String reason; // 신고 사유 (한글)
    private LocalDateTime createdAt; // 신고 일시
    private String statusClass; // CSS 클래스 ("status pending", "status active", "status suspended")
    private String statusDisplay; // 상태 표시 텍스트 ("대기", "처리완료", "거부")

    // 추가 표시 정보
    private String roomName; // 방 이름 (방 신고인 경우)
    private Long roomId; // 방 ID (방 신고인 경우)
    private Long reportedMemberId; // 피신고자 회원 ID
}
