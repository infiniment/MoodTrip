package com.moodTrip.spring.domain.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportActionDto {

    @NotBlank(message = "액션 타입은 필수입니다")
    private String actionType; // "resolve", "reject", "warning", "suspend"

    private String reason; // 처리 사유

    private String message; // 사용자에게 전달할 메시지

    private String adminMemo; // 관리자 내부 메모

    // 경고 관련
    private String warningReason; // 경고 사유
    private String warningMessage; // 경고 메시지

    // 계정 정지 관련
    private Integer suspensionDays; // 정지 일수 (null이면 영구정지)
    private String suspensionReason; // 정지 사유
    private String suspensionMessage; // 정지 통지 메시지
    private Boolean notifyByEmail; // 이메일 알림 여부
    private Boolean notifyByPush; // 푸시 알림 여부

    // 추가 액션
    private Boolean deleteRelatedContent; // 관련 컨텐츠 삭제 여부 (방 신고의 경우 방 삭제)
    private Boolean banFromCreatingRooms; // 방 생성 금지 여부
    private Integer banDurationDays; // 방 생성 금지 기간

    // 유효성 검사를 위한 메서드
    public void validate() {
        if ("warning".equals(actionType)) {
            if (warningMessage == null || warningMessage.trim().isEmpty()) {
                throw new IllegalArgumentException("경고 메시지는 필수입니다");
            }
        }

        if ("suspend".equals(actionType)) {
            if (suspensionMessage == null || suspensionMessage.trim().isEmpty()) {
                throw new IllegalArgumentException("정지 통지 메시지는 필수입니다");
            }
            if (suspensionDays != null && suspensionDays <= 0) {
                throw new IllegalArgumentException("정지 일수는 1일 이상이어야 합니다");
            }
        }
    }

    // 편의 메서드들
    public boolean isResolveAction() {
        return "resolve".equals(actionType);
    }

    public boolean isRejectAction() {
        return "reject".equals(actionType);
    }

    public boolean isWarningAction() {
        return "warning".equals(actionType);
    }

    public boolean isSuspendAction() {
        return "suspend".equals(actionType);
    }

    public boolean isPermanentSuspension() {
        return isSuspendAction() && suspensionDays == null;
    }
}