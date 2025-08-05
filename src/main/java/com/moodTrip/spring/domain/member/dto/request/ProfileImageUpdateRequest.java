package com.moodTrip.spring.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로필 사진 변경 전용 요청 DTO
 * - 기존 ProfileUpdateRequest와 달리 프로필 사진만 변경
 * - API 명세서: PATCH /api/v1/profiles/me/profileImage
 */
@Schema(description = "프로필 사진 변경 요청 DTO")
@Getter
@NoArgsConstructor  // JSON 역직렬화를 위해 필요
@AllArgsConstructor // 모든 필드를 받는 생성자
@Builder            // 빌더 패턴 지원
public class ProfileImageUpdateRequest {

    @Schema(
            description = "새로운 프로필 이미지 URL",
            example = "https://moodtrip-bucket.s3.amazonaws.com/profiles/user123/new-profile.jpg",
            required = true  // 필수 필드임을 명시
    )
    private String profileImage;

}