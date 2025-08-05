package com.moodTrip.spring.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 🚪 로그아웃 응답 DTO
 *
 * 로그아웃 API의 응답 데이터를 담는 클래스입니다.
 * 프론트엔드에서 로그아웃 결과를 확인할 수 있도록 필요한 정보를 제공합니다.
 */
@Schema(description = "로그아웃 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutResponse {

    @Schema(
            description = "로그아웃 성공 여부",
            example = "true",
            required = true
    )
    private boolean success;

    @Schema(
            description = "로그아웃 처리 결과 메시지",
            example = "로그아웃이 완료되었습니다.",
            required = true
    )
    private String message;

    @Schema(
            description = "로그아웃 처리 시간",
            example = "2024-01-15T14:30:00",
            required = true
    )
    private LocalDateTime logoutAt;

    /**
     * 🎯 편의 메서드들 - 컨트롤러에서 쉽게 사용할 수 있도록!
     */

    /**
     * 로그아웃 성공 응답 생성
     */
    public static LogoutResponse success() {
        return LogoutResponse.builder()
                .success(true)
                .message("로그아웃이 완료되었습니다.")
                .logoutAt(LocalDateTime.now())
                .build();
    }

    /**
     * 커스텀 메시지로 성공 응답 생성
     */
    public static LogoutResponse success(String customMessage) {
        return LogoutResponse.builder()
                .success(true)
                .message(customMessage)
                .logoutAt(LocalDateTime.now())
                .build();
    }

    /**
     * 로그아웃 실패 응답 생성
     */
    public static LogoutResponse failure(String errorMessage) {
        return LogoutResponse.builder()
                .success(false)
                .message(errorMessage)
                .logoutAt(LocalDateTime.now())
                .build();
    }
}