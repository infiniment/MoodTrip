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
public class ReportDetailDto {
    // 기본 신고 정보
    private Long reportId;
    private String type; // "ROOM" or "MEMBER"
    private String typeDisplay;
    private String targetSummary;
    private String reporterNickname;
    private String reportedNickname;
    private String reason;
    private String fireMessage; // 신고 내용 상세
    private LocalDateTime createdAt;
    private String statusClass;
    private String statusDisplay;

    // 상세 정보
    private String adminMemo; // 관리자 메모
    private LocalDateTime processedAt; // 처리 일시
    private String processedBy; // 처리자

    // 방 신고 관련 정보
    private String roomName;
    private Long roomId;
    private String roomCreatorNickname;
    private LocalDateTime roomCreatedAt;
    private Boolean isRoomDeleted;

    // 회원 신고 관련 정보
    private Long reportedMemberId;
    private String reportedMemberEmail;
    private String reportedMemberPhone;
    private LocalDateTime reportedMemberJoinDate;
    private String reportedMemberStatus;
    private Integer reportedMemberReportCount; // 해당 회원이 받은 총 신고 수

    // 신고자 정보
    private Long reporterMemberId;
    private String reporterEmail;
    private Integer reporterCredibility; // 신고자 신뢰도 (0-100)

    // 통계 정보
    private Integer similarReportsCount; // 유사한 신고 건수 (최근 30일)
    private Integer reporterTotalReports; // 신고자가 신고한 총 건수
    private Integer reportedMemberTotalReported; // 피신고자가 신고받은 총 건수
}