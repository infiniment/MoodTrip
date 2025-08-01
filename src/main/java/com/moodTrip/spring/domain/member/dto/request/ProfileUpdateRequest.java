package com.moodTrip.spring.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "프로필 수정 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequest {

    @Schema(
            description = "닉네임",
            example = "상우기야미"
    )
    private String nickname;

    @Schema(
            description = "이메일 주소",
            example = "hong@moodtrip.com"
    )
    private String email;

    @Schema(
            description = "휴대폰 번호",
            example = "010-1234-5678"
    )
    private String memberPhone;

    @Schema(
            description = "자기소개 (프로필 바이오)",
            example = "안녕하세요! 여행을 좋아하는 홍길동입니다. 함께 즐거운 여행 떠나요! 🌍✈️"
    )
    private String profileBio;

    @Schema(
            description = "프로필 이미지 URL",
            example = "https://moodtrip-bucket.s3.amazonaws.com/profiles/user123/profile.jpg"
    )
    private String profileImage;
}