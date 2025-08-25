package com.moodTrip.spring.domain.fire.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberFireRequest {

    private String reportedNickname;
    private String reportReason;
    private String reportMessage;

    public void validate() {
        if (reportReason == null || reportReason.isBlank()) {
            throw new IllegalArgumentException("신고 사유를 선택해주세요.");
        }
        if (reportMessage != null && reportMessage.length() > 1000) {
            throw new IllegalArgumentException("신고 내용은 1000자 이내로 작성해주세요.");
        }
    }

    public String getCleanedReportReason() {
        return reportReason != null ? reportReason.trim().toUpperCase() : null;
    }

    public String getCleanedReportMessage() {
        return reportMessage != null ? reportMessage.trim() : "";
    }
}
