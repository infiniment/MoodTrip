package com.moodTrip.spring.domain.fire.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomFireRequest {
    // 신고 사유
    private String reportReason;

    // 신고 내용
    private String reportMessage;

    // 유효성 검사
    public void validate() {
        // 신고 사유는 필수 select 박스 선택하기 때문
        if (reportReason == null || reportReason.trim().isEmpty()) {
            throw new IllegalArgumentException("신고 사유를 선택해주세요.");
        }

        // 유효한 신고 사유인지 체크
        String[] validReasons = {"spam", "inappropriate", "fraud", "harassment", "other"};
        boolean isValidReason = false;
        for (String validReason : validReasons) {
            if (validReason.equalsIgnoreCase(reportReason.trim())) {
                isValidReason = true;
                break;
            }
        }

        if (!isValidReason) {
            throw new IllegalArgumentException("유효하지 않은 신고 사유입니다: " + reportReason);
        }

        // 상세 내용 길이 체크
        if (reportMessage != null && reportMessage.length() > 1000) {
            throw new IllegalArgumentException("신고 내용은 1000자 이내로 작성해주세요.");
        }
    }

    // 신고 사유 정리
    public String getCleanedReportReason() {
        return reportReason != null ? reportReason.trim().toLowerCase() : null;
    }

    // 신고 내용 정리
    public String getCleanedReportMessage() {
        return reportMessage != null ? reportMessage.trim() : "";
    }
}