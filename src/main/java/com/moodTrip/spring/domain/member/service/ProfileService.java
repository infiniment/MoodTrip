package com.moodTrip.spring.domain.member.service;

import com.moodTrip.spring.domain.member.dto.request.IntroduceUpdateRequest;
import com.moodTrip.spring.domain.member.dto.request.ProfileImageUpdateRequest;
import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;
    // 프로필 조회
    public ProfileResponse getMyProfile(Member member) {
        log.info("프로필 조회 요청 - 회원ID: {}", member.getMemberId());

        Optional<Profile> profileOptional = profileRepository.findByMember(member);

        if (profileOptional.isPresent()) {
            // Profile이 존재하는 경우
            Profile profile = profileOptional.get();
            log.info("기존 프로필 조회 성공 - 회원ID: {}, 프로필ID: {}",
                    member.getMemberId(), profile.getProfileId());
            return ProfileResponse.from(profile);
        } else {
            // Profile이 없는 경우, Member 정보만으로 기본 응답 생성
            log.info("프로필이 없음 - Member 정보로 기본 응답 생성 - 회원ID: {}", member.getMemberId());
            return createBasicProfileResponse(member);
        }
    }
    private ProfileResponse createBasicProfileResponse(Member member) {
        return ProfileResponse.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .memberPhone(member.getMemberPhone())
                .profileBio(null)
                .profileImage(null)
                .createdAt(member.getCreatedAt())
                .build();
    }

    // 자기소개 수정 로직
    @Transactional
    public ProfileResponse updateIntroduce(Member member, IntroduceUpdateRequest request) {
        log.info("자기소개 수정 요청 - 회원ID: {}, 새로운 자기소개 길이: {}글자",
                member.getMemberId(),
                request.getProfileBio() != null ? request.getProfileBio().length() : 0);

        // 유효성 검사
        String newIntroduce = request.getProfileBio();
        if (newIntroduce != null && newIntroduce.length() > 500) {
            throw new RuntimeException("자기소개는 500자 이내로 작성해주세요.");
        }

        // 기존 Profile 조회
        Optional<Profile> existingProfile = profileRepository.findByMember(member);

        Profile profile;

        if (existingProfile.isPresent()) {
            // Profile이 있으면 자기소개만 수정
            log.info("기존 Profile 수정 - 회원ID: {}", member.getMemberId());
            profile = existingProfile.get();
            profile.setProfileBio(newIntroduce);
        } else {
            // Profile이 없으면 새로 생성
            log.info("새 Profile 생성 - 회원ID: {} (신규 사용자)", member.getMemberId());
            profile = Profile.builder()
                    .member(member)
                    .profileBio(newIntroduce)
                    .profileImage(null)
                    .build();

            profileRepository.save(profile);
        }

        log.info("자기소개 수정 성공 - 회원ID: {}, Profile ID: {}",
                member.getMemberId(), profile.getProfileId());

        return ProfileResponse.from(profile);
    }
    // 프로필 사진 변경 로직
    @Transactional
    public ProfileResponse updateProfileImage(Member member, ProfileImageUpdateRequest request) {
        log.info("프로필 사진 변경 요청 - 회원ID: {}, 새이미지URL: {}",
                member.getMemberId(), request.getProfileImage());

        // 유효성 검사
        String newImageUrl = request.getProfileImage();
        if (newImageUrl == null || newImageUrl.trim().isEmpty()) {
            throw new RuntimeException("프로필 이미지 URL은 필수 입력 항목입니다.");
        }

        newImageUrl = newImageUrl.trim();

        // URL 형식 검증
        if (!isValidImageUrl(newImageUrl)) {
            throw new RuntimeException("올바른 이미지 URL 형식이 아닙니다.");
        }

        // Profile 조회 또는 생성
        Optional<Profile> existingProfile = profileRepository.findByMember(member);
        Profile profile;

        if (existingProfile.isPresent()) {
            // 기존 Profile 수정
            profile = existingProfile.get();
            log.info("기존 Profile의 이미지 수정 - 회원ID: {}", member.getMemberId());
        } else {
            // Profile이 없으면 새로 생성 (프로필 이미지만 설정)
            log.info("새 Profile 생성 (프로필 이미지) - 회원ID: {}", member.getMemberId());
            profile = Profile.builder()
                    .member(member)
                    .profileBio(null)        // 자기소개는 나중에 설정
                    .profileImage(newImageUrl)
                    .build();

            profileRepository.save(profile);
            log.info("새 Profile 저장 완료 - 회원ID: {}, Profile ID: {}",
                    member.getMemberId(), profile.getProfileId());

            return ProfileResponse.from(profile);
        }

        // 기존 Profile의 이미지만 수정
        String oldImageUrl = profile.getProfileImage();
        profile.setProfileImage(newImageUrl);

        log.info("프로필 사진 변경 성공 - 회원ID: {}, 기존URL: {}, 신규URL: {}",
                member.getMemberId(), oldImageUrl, newImageUrl);

        return ProfileResponse.from(profile);
    }


    // 이미지 URL 유효성 검사 메서드 분리
    private boolean isValidImageUrl(String imageUrl) {
        return imageUrl.startsWith("http://") ||
                imageUrl.startsWith("https://") ||
                imageUrl.startsWith("/uploads/") ||
                imageUrl.startsWith("/static/image/");
    }

    /**
     * 🔥 프로필 존재 여부 확인 (다른 서비스에서 사용할 수 있는 유틸리티)
     */
    public boolean hasProfile(Member member) {
        return profileRepository.findByMember(member).isPresent();
    }
    // 프로필은 로그인 후 생기는 메서드가 존재하지 않음
    // 해당 코드를 활용하여 member 테이블 생성 시 프로필 테이블 생성.
    @Transactional
    public Profile createDefaultProfile(Member member) {
        log.info("기본 Profile 생성 - 회원ID: {}", member.getMemberId());

        Profile profile = Profile.builder()
                .member(member)
                .profileBio(null)
                .profileImage(null)
                .build();

        Profile savedProfile = profileRepository.save(profile);
        log.info("기본 Profile 생성 완료 - 회원ID: {}, Profile ID: {}",
                member.getMemberId(), savedProfile.getProfileId());

        return savedProfile;
    }
}