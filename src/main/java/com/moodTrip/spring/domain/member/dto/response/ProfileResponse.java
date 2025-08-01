package com.moodTrip.spring.domain.member.dto.response;

import com.moodTrip.spring.domain.member.entity.Profile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "프로필 조회/수정 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {

    @Schema(
            description = "회원 닉네임",
            example = "상우기야미"
    )
    private String nickname;

    @Schema(
            description = "자기소개",
            example = "나는 기염둥이 상우"
    )
    private String profileBio;

    @Schema(
            description = "프로필 이미지 URL",
            example = "https://moodtrip-bucket.s3.amazonaws.com/profiles/user123/profile.jpg"
    )
    private String profileImage;

    @Schema(
            description = "이메일 주소",
            example = "sangwoo@moodtrip.com"
    )
    private String email;

    @Schema(
            description = "휴대폰 번호",
            example = "010-1234-5678"
    )
    private String memberPhone;

    @Schema(
            description = "프로필 생성일시",
            example = "2024-01-15T10:30:00"
    )
    private LocalDateTime createdAt;

    // profile 엔티티를 profileResponse로 변환하는 정적 메서드
    public static ProfileResponse from(Profile profile) {
        return ProfileResponse.builder()
                .nickname(profile.getMember().getNickname())
                .profileBio(profile.getProfileBio())
                .profileImage(profile.getProfileImage())
                .email(profile.getMember().getEmail())
                .memberPhone(profile.getMember().getMemberPhone())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}