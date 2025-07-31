package com.moodTrip.spring.domain.Member.dto;

import com.moodTrip.spring.domain.member.dto.request.NicknameUpdateRequest;
import com.moodTrip.spring.domain.member.dto.request.ProfileUpdateRequest;
import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * 테스트 하는 것들
 * 1. DTO 생성과 getter 메서드 테스트
 * 2. ProfileResponse.from() 변환 메서드 테스트
 * 3. Builder 패턴 테스트
 */
@DisplayName("DTO 단위테스트")
class DtoUnitTest {

    // ========================================
    // 🧪 1. NicknameUpdateRequest 테스트
    // ========================================

    @Test
    @DisplayName("NicknameUpdateRequest 생성 및 getter 테스트")
    void nicknameUpdateRequest_생성_및_getter() {
        // Given
        String expectedNickname = "테스트닉네임";

        // When
        NicknameUpdateRequest request = new NicknameUpdateRequest(expectedNickname);

        // Then
        assertThat(request.getNickname()).isEqualTo(expectedNickname);
    }

    @Test
    @DisplayName("NicknameUpdateRequest null 값 처리")
    void nicknameUpdateRequest_null값처리() {
        // When
        NicknameUpdateRequest request = new NicknameUpdateRequest(null);

        // Then
        assertThat(request.getNickname()).isNull();
    }

    @Test
    @DisplayName("NicknameUpdateRequest 빈 문자열 처리")
    void nicknameUpdateRequest_빈문자열처리() {
        // When
        NicknameUpdateRequest request = new NicknameUpdateRequest("");

        // Then
        assertThat(request.getNickname()).isEqualTo("");
    }

    // ========================================
    // 🧪 2. ProfileUpdateRequest 테스트
    // ========================================

    @Test
    @DisplayName("ProfileUpdateRequest Builder 패턴 테스트")
    void profileUpdateRequest_빌더패턴_테스트() {
        // Given & When
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("테스트닉네임")
                .email("test@example.com")
                .memberPhone("010-1234-5678")
                .profileBio("테스트 자기소개")
                .profileImage("test-image.jpg")
                .build();

        // Then
        assertThat(request.getNickname()).isEqualTo("테스트닉네임");
        assertThat(request.getEmail()).isEqualTo("test@example.com");
        assertThat(request.getMemberPhone()).isEqualTo("010-1234-5678");
        assertThat(request.getProfileBio()).isEqualTo("테스트 자기소개");
        assertThat(request.getProfileImage()).isEqualTo("test-image.jpg");
    }

    @Test
    @DisplayName("ProfileUpdateRequest 부분적 빌딩 테스트")
    void profileUpdateRequest_부분적빌딩_테스트() {
        // Given & When - 일부 필드만 설정
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("닉네임만설정")
                .email("email@test.com")
                // profileBio, profileImage는 설정하지 않음
                .build();

        // Then
        assertThat(request.getNickname()).isEqualTo("닉네임만설정");
        assertThat(request.getEmail()).isEqualTo("email@test.com");
        assertThat(request.getMemberPhone()).isNull(); // 설정하지 않은 필드는 null
        assertThat(request.getProfileBio()).isNull();
        assertThat(request.getProfileImage()).isNull();
    }

    // ========================================
    // 🧪 3. ProfileResponse 테스트
    // ========================================

    @Test
    @DisplayName("ProfileResponse Builder 패턴 테스트")
    void profileResponse_빌더패턴_테스트() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        ProfileResponse response = ProfileResponse.builder()
                .nickname("테스트닉네임")
                .profileBio("테스트 바이오")
                .profileImage("test.jpg")
                .email("test@example.com")
                .memberPhone("010-1234-5678")
                .createdAt(now)
                .build();

        // Then
        assertThat(response.getNickname()).isEqualTo("테스트닉네임");
        assertThat(response.getProfileBio()).isEqualTo("테스트 바이오");
        assertThat(response.getProfileImage()).isEqualTo("test.jpg");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getMemberPhone()).isEqualTo("010-1234-5678");
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("ProfileResponse.from() 변환 메서드 테스트")
    void profileResponse_from메서드_테스트() {
        // Given - Member 엔티티 생성
        Member member = Member.builder()
                .memberPk(1L)
                .memberId("testuser")
                .memberName("테스트유저")
                .email("test@example.com")
                .memberPhone("010-1234-5678")
                .memberAuth("U")
                .isWithdraw(false)
                .build();

        // Profile 엔티티 생성
        Profile profile = Profile.builder()
                .profileId(1L)
                .member(member)
                .nickname("프로필닉네임")
                .profileBio("프로필 자기소개")
                .profileImage("profile-image.jpg")
                .build();

        // When - ProfileResponse로 변환
        ProfileResponse response = ProfileResponse.from(profile);

        // Then - 모든 필드가 올바르게 변환되었는지 확인
        assertThat(response).isNotNull();
        assertThat(response.getNickname()).isEqualTo("프로필닉네임"); // ✅ Profile의 nickname 사용
        assertThat(response.getProfileBio()).isEqualTo("프로필 자기소개");
        assertThat(response.getProfileImage()).isEqualTo("profile-image.jpg");
        assertThat(response.getEmail()).isEqualTo("test@example.com"); // Member의 이메일
        assertThat(response.getMemberPhone()).isEqualTo("010-1234-5678"); // Member의 전화번호
        assertThat(response.getCreatedAt()).isEqualTo(profile.getCreatedAt()); // Profile의 생성일시
    }

    @Test
    @DisplayName("ProfileResponse.from() null 필드 처리 테스트")
    void profileResponse_from메서드_null필드처리() {
        // Given - 일부 필드가 null인 엔티티
        Member member = Member.builder()
                .memberPk(1L)
                .memberId("testuser")
                .memberName("테스트유저")
                .email(null) // null 이메일
                .memberPhone(null) // null 전화번호
                .memberAuth("U")
                .isWithdraw(false)
                .build();

        Profile profile = Profile.builder()
                .profileId(1L)
                .member(member)
                .nickname(null) // null 닉네임
                .profileBio(null) // null 자기소개
                .profileImage(null) // null 이미지
                .build();

        // When
        ProfileResponse response = ProfileResponse.from(profile);

        // Then - null 값들이 올바르게 처리되는지 확인
        assertThat(response).isNotNull();
        assertThat(response.getNickname()).isNull(); // ✅ Profile의 nickname이 null이므로 null
        assertThat(response.getProfileBio()).isNull();
        assertThat(response.getProfileImage()).isNull();
        assertThat(response.getEmail()).isNull();
        assertThat(response.getMemberPhone()).isNull();
    }

    // ========================================
    // 🧪 4. 극단값 및 경계값 테스트
    // ========================================

    @Test
    @DisplayName("매우 긴 문자열 처리 테스트")
    void dto_매우긴문자열_처리() {
        // Given
        String longString = "a".repeat(10000); // 10000자 문자열

        // When
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname(longString)
                .profileBio(longString)
                .build();

        // Then
        assertThat(request.getNickname()).hasSize(10000);
        assertThat(request.getProfileBio()).hasSize(10000);
    }

    @Test
    @DisplayName("특수문자 포함 문자열 처리 테스트")
    void dto_특수문자_처리() {
        // Given
        String specialString = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~한글";

        // When
        NicknameUpdateRequest request = new NicknameUpdateRequest(specialString);

        // Then
        assertThat(request.getNickname()).isEqualTo(specialString);
    }

    @Test
    @DisplayName("이모지 포함 문자열 처리 테스트")
    void dto_이모지_처리() {
        // Given
        String emojiString = "안녕하세요! 🌟🎉🚀😊";

        // When
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .profileBio(emojiString)
                .build();

        // Then
        assertThat(request.getProfileBio()).isEqualTo(emojiString);
    }
}