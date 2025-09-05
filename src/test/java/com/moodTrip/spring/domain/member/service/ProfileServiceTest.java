//package com.moodTrip.spring.domain.Member.Service;
//
//import com.moodTrip.spring.domain.member.dto.request.NicknameUpdateRequest;
//import com.moodTrip.spring.domain.member.dto.request.ProfileUpdateRequest;
//import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
//import com.moodTrip.spring.domain.member.entity.Member;
//import com.moodTrip.spring.domain.member.entity.Profile;
//import com.moodTrip.spring.domain.member.repository.ProfileRepository;
//import com.moodTrip.spring.domain.member.service.ProfileService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.*;
//
///**
// * 🧪 ProfileService 단위테스트
// *
// * 🎯 테스트 목표:
// * 1. 프로필 조회 기능 테스트
// * 2. 프로필 수정 기능 테스트
// * 3. 닉네임 수정 기능 테스트
// * 4. 예외 상황 처리 테스트
// *
// * 📚 단위테스트 원칙:
// * - Mock을 사용해서 외부 의존성(Repository) 제거
// * - 서비스 로직만 순수하게 테스트
// * - 빠르고 독립적인 테스트
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("ProfileService 단위테스트")
//class ProfileServiceTest {
//
//    @Mock
//    private ProfileRepository profileRepository;
//
//    @InjectMocks
//    private ProfileService profileService;
//
//    // 테스트용 더미 데이터
//    private Member testMember;
//    private Profile testProfile;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트용 Member 생성
//        testMember = Member.builder()
//                .memberPk(1L)
//                .memberId("testuser123")
//                .memberName("테스트유저")
//                .email("test@moodtrip.com")
//                .memberPhone("010-1234-5678")
//                .memberAuth("U")
//                .isWithdraw(false)
//                .build();
//
//        // 테스트용 Profile 생성
//        testProfile = Profile.builder()
//                .profileId(1L)
//                .member(testMember)
//                .nickname("기존닉네임")
//                .profileBio("안녕하세요! 테스트 프로필입니다.")
//                .profileImage("test-profile.jpg")
//                .build();
//    }
//
//    // ========================================
//    // 🧪 1. 프로필 조회 테스트
//    // ========================================
//
//    @Test
//    @DisplayName("프로필 조회 성공 - 프로필이 존재하는 경우")
//    void getMyProfile_프로필존재_성공() {
//        // Given
//        given(profileRepository.findByMember(testMember))
//                .willReturn(Optional.of(testProfile));
//
//        // When
//        ProfileResponse result = profileService.getMyProfile(testMember);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getNickname()).isEqualTo("기존닉네임");
//        assertThat(result.getProfileBio()).isEqualTo("안녕하세요! 테스트 프로필입니다.");
//        assertThat(result.getEmail()).isEqualTo("test@moodtrip.com");
//        assertThat(result.getMemberPhone()).isEqualTo("010-1234-5678");
//        assertThat(result.getProfileImage()).isEqualTo("test-profile.jpg");
//
//        // Repository 메서드가 정확히 1번 호출되었는지 검증
//        then(profileRepository).should(times(1)).findByMember(testMember);
//    }
//
//    @Test
//    @DisplayName("프로필 조회 - 프로필이 없으면 더미 데이터 반환")
//    void getMyProfile_프로필없음_더미데이터반환() {
//        // Given
//        given(profileRepository.findByMember(testMember))
//                .willReturn(Optional.empty());
//
//        // When
//        ProfileResponse result = profileService.getMyProfile(testMember);
//
//        // Then - 더미 데이터가 반환되는지 확인
//        assertThat(result).isNotNull();
//        assertThat(result.getNickname()).isEqualTo("테스트유저"); // Member의 이름이 닉네임으로
//        assertThat(result.getEmail()).isEqualTo("test@moodtrip.com");
//        assertThat(result.getMemberPhone()).isEqualTo("010-1234-5678");
//        assertThat(result.getProfileBio()).contains("여행을 좋아하는"); // 더미 바이오 포함
//        assertThat(result.getCreatedAt()).isNotNull();
//
//        then(profileRepository).should(times(1)).findByMember(testMember);
//    }
//
//    // ========================================
//    // 🧪 2. 닉네임 수정 테스트
//    // ========================================
//
//    @Test
//    @DisplayName("닉네임 수정 성공")
//    void updateNickname_성공() {
//        // Given
//        given(profileRepository.findByMember(testMember))
//                .willReturn(Optional.of(testProfile));
//
//        NicknameUpdateRequest request = new NicknameUpdateRequest("새로운닉네임");
//
//        // When
//        ProfileResponse result = profileService.updateNickname(testMember, request);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getNickname()).isEqualTo("새로운닉네임");
//
//        // 실제 Profile 엔티티의 닉네임도 변경되었는지 확인
//        assertThat(testProfile.getNickname()).isEqualTo("새로운닉네임");
//
//        // 다른 필드들은 그대로인지 확인
//        assertThat(result.getEmail()).isEqualTo("test@moodtrip.com");
//        assertThat(result.getProfileBio()).isEqualTo("안녕하세요! 테스트 프로필입니다.");
//
//        then(profileRepository).should(times(1)).findByMember(testMember);
//    }
//
//    @Test
//    @DisplayName("닉네임 수정 - null 값으로 수정")
//    void updateNickname_null값으로_수정() {
//        // Given
//        given(profileRepository.findByMember(testMember))
//                .willReturn(Optional.of(testProfile));
//
//        NicknameUpdateRequest request = new NicknameUpdateRequest(null);
//
//        // When
//        ProfileResponse result = profileService.updateNickname(testMember, request);
//
//        // Then
//        assertThat(result.getNickname()).isNull();
//        assertThat(testProfile.getNickname()).isNull();
//    }
//
//    @Test
//    @DisplayName("닉네임 수정 - 빈 문자열로 수정")
//    void updateNickname_빈문자열로_수정() {
//        // Given
//        given(profileRepository.findByMember(testMember))
//                .willReturn(Optional.of(testProfile));
//
//        NicknameUpdateRequest request = new NicknameUpdateRequest("");
//
//        // When
//        ProfileResponse result = profileService.updateNickname(testMember, request);
//
//        // Then
//        assertThat(result.getNickname()).isEqualTo("");
//        assertThat(testProfile.getNickname()).isEqualTo("");
//    }
//
//    @Test
//    @DisplayName("닉네임 수정 실패 - 프로필이 존재하지 않음")
//    void updateNickname_프로필없음_예외발생() {
//        // Given
//        given(profileRepository.findByMember(testMember))
//                .willReturn(Optional.empty());
//
//        NicknameUpdateRequest request = new NicknameUpdateRequest("새로운닉네임");
//
//        // When & Then
//        assertThatThrownBy(() -> profileService.updateNickname(testMember, request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("프로필을 찾을 수 없습니다");
//
//        then(profileRepository).should(times(1)).findByMember(testMember);
//    }
//
//    // ========================================
//    // 🧪 3. 전체 프로필 수정 테스트
//    // ========================================
//
//    @Test
//    @DisplayName("프로필 수정 성공 - 모든 필드 수정")
//    void updateMyProfile_모든필드수정_성공() {
//        // Given
//        given(profileRepository.findByMember(testMember))
//                .willReturn(Optional.of(testProfile));
//
//        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
//                .nickname("수정된닉네임")
//                .email("updated@moodtrip.com")
//                .memberPhone("010-9999-8888")
//                .profileBio("수정된 자기소개입니다!")
//                .profileImage("updated-profile.jpg")
//                .build();
//
//        // When
//        ProfileResponse result = profileService.updateMyProfile(testMember, request);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getNickname()).isEqualTo("수정된닉네임");
//        assertThat(result.getEmail()).isEqualTo("updated@moodtrip.com");
//        assertThat(result.getMemberPhone()).isEqualTo("010-9999-8888");
//        assertThat(result.getProfileBio()).isEqualTo("수정된 자기소개입니다!");
//        assertThat(result.getProfileImage()).isEqualTo("updated-profile.jpg");
//
//        // 실제 엔티티도 수정되었는지 확인
//        assertThat(testProfile.getNickname()).isEqualTo("수정된닉네임");
//        assertThat(testProfile.getProfileBio()).isEqualTo("수정된 자기소개입니다!");
//        assertThat(testProfile.getProfileImage()).isEqualTo("updated-profile.jpg");
//        assertThat(testProfile.getMember().getEmail()).isEqualTo("updated@moodtrip.com");
//        assertThat(testProfile.getMember().getMemberPhone()).isEqualTo("010-9999-8888");
//
//        then(profileRepository).should(times(1)).findByMember(testMember);
//    }
//
//    @Test
//    @DisplayName("프로필 수정 - 일부 필드만 수정 (null 값 포함)")
//    void updateMyProfile_일부필드만수정() {
//        // Given
//        given(profileRepository.findByMember(testMember))
//                .willReturn(Optional.of(testProfile));
//
//        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
//                .nickname("새닉네임")
//                .email(null)  // null로 설정
//                .memberPhone("010-9999-8888")
//                .profileBio(null)  // null로 설정
//                .profileImage("new-image.jpg")
//                .build();
//
//        // When
//        ProfileResponse result = profileService.updateMyProfile(testMember, request);
//
//        // Then
//        assertThat(result.getNickname()).isEqualTo("새닉네임");
//        assertThat(result.getEmail()).isNull();
//        assertThat(result.getMemberPhone()).isEqualTo("010-9999-8888");
//        assertThat(result.getProfileBio()).isNull();
//        assertThat(result.getProfileImage()).isEqualTo("new-image.jpg");
//    }
//
//    @Test
//    @DisplayName("프로필 수정 실패 - 프로필이 존재하지 않음")
//    void updateMyProfile_프로필없음_예외발생() {
//        // Given
//        given(profileRepository.findByMember(testMember))
//                .willReturn(Optional.empty());
//
//        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
//                .nickname("수정된닉네임")
//                .email("updated@moodtrip.com")
//                .build();
//
//        // When & Then
//        assertThatThrownBy(() -> profileService.updateMyProfile(testMember, request))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("프로필을 찾을 수 없습니다");
//
//        then(profileRepository).should(times(1)).findByMember(testMember);
//    }
//
//    // ========================================
//    // 🧪 4. 경계값 테스트
//    // ========================================
//
//    @Test
//    @DisplayName("극단적인 값으로 테스트 - 매우 긴 닉네임")
//    void updateNickname_매우긴닉네임() {
//        // Given
//        given(profileRepository.findByMember(testMember))
//                .willReturn(Optional.of(testProfile));
//
//        String longNickname = "a".repeat(1000); // 1000자 닉네임
//        NicknameUpdateRequest request = new NicknameUpdateRequest(longNickname);
//
//        // When
//        ProfileResponse result = profileService.updateNickname(testMember, request);
//
//        // Then
//        assertThat(result.getNickname()).isEqualTo(longNickname);
//        assertThat(testProfile.getNickname()).isEqualTo(longNickname);
//    }
//
//    @Test
//    @DisplayName("특수문자가 포함된 닉네임으로 수정")
//    void updateNickname_특수문자포함() {
//        // Given
//        given(profileRepository.findByMember(testMember))
//                .willReturn(Optional.of(testProfile));
//
//        String specialNickname = "닉네임@#$%^&*()[]{}";
//        NicknameUpdateRequest request = new NicknameUpdateRequest(specialNickname);
//
//        // When
//        ProfileResponse result = profileService.updateNickname(testMember, request);
//
//        // Then
//        assertThat(result.getNickname()).isEqualTo(specialNickname);
//    }
//}
package com.moodTrip.spring.domain.member.service;

import com.moodTrip.spring.domain.attraction.dto.response.AttractionSearchResponseDto;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.entity.UserAttraction;
import com.moodTrip.spring.domain.attraction.repository.AttractionRepository;
import com.moodTrip.spring.domain.attraction.repository.UserAttractionRepository;
import com.moodTrip.spring.domain.member.dto.request.IntroduceUpdateRequest;
import com.moodTrip.spring.domain.member.dto.request.ProfileImageUpdateRequest;
import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserAttractionRepository userAttractionRepository;

    @Mock
    private AttractionRepository attractionRepository;

    @InjectMocks
    private ProfileService profileService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId("user1")
                .nickname("nickname")
                .email("email@example.com")
                .memberPhone("010-0000-0000")
                .createdAt(LocalDateTime.now())
                .build();
    }



    @Test
    @DisplayName("프로필 조회 - 기존 Profile 있으면 응답 반환")
    void testGetMyProfile_ExistingProfile() {
        Profile profile = Profile.builder()
                .profileId(1L)
                .member(member)
                .profileBio("Hello")
                .profileImage(null)
                .build();

        when(profileRepository.findByMember(member)).thenReturn(Optional.of(profile));

        ProfileResponse response = profileService.getMyProfile(member);

        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileBio()).isEqualTo("Hello");
        verify(profileRepository, times(1)).findByMember(member);
    }

    @Test
    @DisplayName("프로필 조회 - Profile 없으면 기본 응답 생성")
    void testGetMyProfile_NoProfile() {
        when(profileRepository.findByMember(member)).thenReturn(Optional.empty());

        ProfileResponse response = profileService.getMyProfile(member);

        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getProfileImage()).isEqualTo("/image/fix/moodtrip.png");
    }

    @Test
    @DisplayName("자기소개 수정 - 길이 초과하면 예외 발생")
    void testUpdateIntroduce_BioTooLong() {
        IntroduceUpdateRequest req = new IntroduceUpdateRequest();
        req.setProfileBio("a".repeat(501)); // 501글자

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> profileService.updateIntroduce(member, req));

        assertThat(ex).hasMessageContaining("500자 이내");
    }

    @Test
    @DisplayName("자기소개 수정 - 기존 Profile 있으면 수정")
    void testUpdateIntroduce_ExistingProfile() {
        IntroduceUpdateRequest req = new IntroduceUpdateRequest();
        req.setProfileBio("New intro");

        Profile profile = Profile.builder()
                .profileId(1L)
                .member(member)
                .profileBio("Old intro")
                .build();

        when(profileRepository.findByMember(member)).thenReturn(Optional.of(profile));

        ProfileResponse response = profileService.updateIntroduce(member, req);

        verify(profileRepository, never()).save(any(Profile.class)); // 기존 프로필이므로 save 안 함

        assertThat(profile.getProfileBio()).isEqualTo("New intro");
        assertThat(response.getProfileBio()).isEqualTo("New intro");
    }

    @Test
    @DisplayName("자기소개 수정 - 기존 Profile 없으면 새로 생성 후 저장")
    void testUpdateIntroduce_NewProfile() {
        IntroduceUpdateRequest req = new IntroduceUpdateRequest();
        req.setProfileBio("New intro");

        when(profileRepository.findByMember(member)).thenReturn(Optional.empty());

        Profile savedProfile = Profile.builder()
                .profileId(10L)
                .member(member)
                .profileBio("New intro")
                .build();

        when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);

        ProfileResponse response = profileService.updateIntroduce(member, req);

        verify(profileRepository, times(1)).save(any(Profile.class));
        // .profileId() 검증 제거
        assertThat(response.getProfileBio()).isEqualTo("New intro");
    }

    @Test
    @DisplayName("프로필 사진 변경 - 유효하지 않은 URL에 대해 예외 발생")
    void testUpdateProfileImage_InvalidUrl() {
        ProfileImageUpdateRequest req = new ProfileImageUpdateRequest("invalid_url");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> profileService.updateProfileImage(member, req));

        assertThat(ex).hasMessageContaining("올바른 이미지 URL 형식이 아닙니다");
    }

    @Test
    @DisplayName("프로필 사진 변경 - 기존 프로필 없으면 새로 생성")
    void testUpdateProfileImage_CreateNew() {
        ProfileImageUpdateRequest req = new ProfileImageUpdateRequest("/uploads/image.png");

        when(profileRepository.findByMember(member)).thenReturn(Optional.empty());

        Profile savedProfile = Profile.builder()
                .profileId(5L)
                .member(member)
                .profileImage("/uploads/image.png")
                .build();

        when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);

        ProfileResponse response = profileService.updateProfileImage(member, req);

        verify(profileRepository, times(1)).save(any(Profile.class));
        assertThat(response.getProfileImage()).isEqualTo("/uploads/image.png");
    }

    @Test
    @DisplayName("프로필 사진 변경 - 기존 프로필 있으면 이미지 수정")
    void testUpdateProfileImage_Existing() {
        ProfileImageUpdateRequest req = new ProfileImageUpdateRequest("/static/image/new.png");

        Profile existingProfile = Profile.builder()
                .profileId(7L)
                .member(member)
                .profileImage("/static/image/old.png")
                .build();

        when(profileRepository.findByMember(member)).thenReturn(Optional.of(existingProfile));

        ProfileResponse response = profileService.updateProfileImage(member, req);

        assertThat(existingProfile.getProfileImage()).isEqualTo("/static/image/new.png");
        assertThat(response.getProfileImage()).isEqualTo("/static/image/new.png");
    }

    @Test
    @DisplayName("찜 목록 조회 - 페이지 처리 및 변환 테스트")
    void testGetMyWishlist() {
        Long memberPk = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Attraction attraction = Attraction.builder().contentId(100L).title("Attraction1").build();
        UserAttraction ua = UserAttraction.builder()
                .member(member)
                .attraction(attraction)
                .build();

        List<UserAttraction> userAttractionList = List.of(ua);
        Page<UserAttraction> userAttractionPage = new PageImpl<>(userAttractionList, pageable, userAttractionList.size());

        when(userAttractionRepository.findByMemberMemberPkWithAttraction(memberPk, pageable))
                .thenReturn(userAttractionPage);

        Page<AttractionSearchResponseDto> result = profileService.getMyWishlist(memberPk, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        AttractionSearchResponseDto dto = result.getContent().get(0);
        assertThat(dto.getContentId()).isEqualTo(100L);
        assertThat(dto.isLikedByCurrentUser()).isTrue();
    }

    @Test
    @DisplayName("찜 목록 삭제 테스트")
    void testRemoveLike() {
        Long memberPk = 1L;
        Long attractionId = 10L;

        Member foundMember = member;
        Attraction foundAttraction = Attraction.builder().contentId(attractionId).build();

        when(memberRepository.findById(memberPk)).thenReturn(Optional.of(foundMember));
        when(attractionRepository.findById(attractionId)).thenReturn(Optional.of(foundAttraction));

        profileService.removeLike(memberPk, attractionId);

        verify(userAttractionRepository, times(1)).deleteByMemberAndAttraction(foundMember, foundAttraction);
    }
}
