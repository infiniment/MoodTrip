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

    // 1. 프로필 조회
    public ProfileResponse getMyProfile(Member member) {
        log.info("프로필 조회 요청 - 회원ID: {}", member.getMemberId());

        Optional<Profile> profileOptional = profileRepository.findByMember(member);

        if (profileOptional.isPresent()) {
            Profile profile = profileOptional.get();
            log.info("프로필 조회 성공 - 회원ID: {}, 프로필ID: {}",
                    member.getMemberId(), profile.getProfileId());
            return ProfileResponse.from(profile);
        } else {
            // 예외 던지기 대신 더미 데이터 반환으로 변경!
            log.warn("프로필이 존재하지 않음 - 더미 데이터 반환 - 회원ID: {}", member.getMemberId());
            return createDummyProfile(member);
        }
    }

    // 더미 프로필 생성 메서드 추가
    private ProfileResponse createDummyProfile(Member member) {
        return ProfileResponse.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .memberPhone(member.getMemberPhone())
                .profileBio("안녕하세요! 여행을 좋아하는 " + member.getNickname() + "입니다. 함께 즐거운 여행 떠나요! 🌍✈️")
                .profileImage(null)
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 자기소개 수정 메서드
     * - Profile이 없으면 새로 생성
     * - Profile이 있으면 profileBio만 수정
     */
    @Transactional
    public ProfileResponse updateIntroduce(Member member, IntroduceUpdateRequest request) {
        log.info("자기소개 수정 요청 - 회원ID: {}, 새로운 자기소개 길이: {}글자",
                member.getMemberId(),
                request.getProfileBio() != null ? request.getProfileBio().length() : 0);

        // ✅ 1단계: 유효성 검사
        String newIntroduce = request.getProfileBio();
        if (newIntroduce != null && newIntroduce.length() > 500) {
            throw new RuntimeException("자기소개는 500자 이내로 작성해주세요.");
        }

        // ✅ 2단계: 기존 Profile 조회
        Optional<Profile> existingProfile = profileRepository.findByMember(member);

        Profile profile;

        if (existingProfile.isPresent()) {
            // ✅ 3-1단계: Profile이 있으면 자기소개만 수정
            log.info("기존 Profile 수정 - 회원ID: {}", member.getMemberId());
            profile = existingProfile.get();
            profile.setProfileBio(newIntroduce);
        } else {
            // ✅ 3-2단계: Profile이 없으면 새로 생성
            log.info("새 Profile 생성 - 회원ID: {}", member.getMemberId());
            profile = Profile.builder()
                    .member(member)              // Member와 연결
                    .profileBio(newIntroduce)    // 자기소개 설정
                    .profileImage(null)          // 프로필 이미지는 일단 null
                    .build();

            // 새로 생성한 Profile 저장
            profileRepository.save(profile);
        }

        log.info("자기소개 수정 성공 - 회원ID: {}, Profile ID: {}",
                member.getMemberId(), profile.getProfileId());

        // ✅ 4단계: 수정된 Profile 정보를 ProfileResponse로 변환해서 반환
        return ProfileResponse.from(profile);
    }

    /**
     * 🔥 새로 추가: 프로필 사진 변경
     * - 기존 updateMyProfile()과 달리 프로필 사진만 수정
     * - 유효성 검사 포함
     */
    @Transactional  // 데이터베이스 트랜잭션 처리
    public ProfileResponse updateProfileImage(Member member, ProfileImageUpdateRequest request) {
        log.info("프로필 사진 변경 요청 - 회원ID: {}, 새이미지URL: {}",
                member.getMemberId(), request.getProfileImage());

        // 1️⃣ 유효성 검사
        String newImageUrl = request.getProfileImage();
        if (newImageUrl == null || newImageUrl.trim().isEmpty()) {
            throw new RuntimeException("프로필 이미지 URL은 필수 입력 항목입니다.");
        }

        newImageUrl = newImageUrl.trim();

        // URL 형식 간단 검증 (실제로는 더 정교한 검증 필요)
        if (!newImageUrl.startsWith("http://") &&
                !newImageUrl.startsWith("https://") &&
                !newImageUrl.startsWith("/uploads/")) {
            throw new RuntimeException("올바른 이미지 URL 형식이 아닙니다.");
        }


        // 2️⃣ Profile 조회
        Profile profile = profileRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.error("프로필 사진 변경 실패 - 회원ID: {}, 프로필이 존재하지 않음", member.getMemberId());
                    return new RuntimeException("프로필을 찾을 수 없습니다.");
                });

        // 3️⃣ 프로필 사진만 수정 (다른 필드는 건드리지 않음)
        String oldImageUrl = profile.getProfileImage();
        profile.setProfileImage(newImageUrl);


        log.info("프로필 사진 변경 성공 - 회원ID: {}, 기존URL: {}, 신규URL: {}",
                member.getMemberId(), oldImageUrl, newImageUrl);

        return ProfileResponse.from(profile);
    }

}