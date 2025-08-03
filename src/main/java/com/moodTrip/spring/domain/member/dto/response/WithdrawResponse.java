package com.moodTrip.spring.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "회원 탈퇴 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawResponse {

    @Schema(
            description = "탈퇴된 회원 ID",
            example = "testuser123"
    )
    private String memberId;

    @Schema(
            description = "탈퇴 처리 시간",
            example = "2024-01-15T14:30:00"
    )
    private LocalDateTime withdrawnAt;

    @Schema(
            description = "처리 결과 메시지",
            example = "탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다."
    )
    private String message;

    @Schema(
            description = "성공 여부",
            example = "true"
    )
    private boolean success;
}