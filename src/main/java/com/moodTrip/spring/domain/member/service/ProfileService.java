package com.moodTrip.spring.domain.member.service;

import com.moodTrip.spring.domain.member.dto.request.NicknameUpdateRequest;
import com.moodTrip.spring.domain.member.dto.request.ProfileUpdateRequest;
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
                .nickname(member.getMemberName())
                .email(member.getEmail())
                .memberPhone(member.getMemberPhone())
                .profileBio("안녕하세요! 여행을 좋아하는 " + member.getMemberName() + "입니다. 함께 즐거운 여행 떠나요! 🌍✈️")
                .profileImage(null)
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }

    @Transactional
    public ProfileResponse updateMyProfile(Member member, ProfileUpdateRequest request) {
        log.info("프로필 수정 요청 - 회원ID: {}", member.getMemberId());

        // 1. Profile 조회
        Profile profile = profileRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.error("프로필 수정 실패 - 회원ID: {}, 프로필이 존재하지 않음", member.getMemberId());
                    return new RuntimeException("프로필을 찾을 수 없습니다.");
                });

        // 2. Member 정보 수정
        Member memberToUpdate = profile.getMember();
        memberToUpdate.setEmail(request.getEmail());
        memberToUpdate.setMemberPhone(request.getMemberPhone());

        // 3. Profile 정보 수정
        profile.setNickname(request.getNickname());
        profile.setProfileBio(request.getProfileBio());
        profile.setProfileImage(request.getProfileImage());

        log.info("프로필 수정 성공 - 회원ID: {}, 닉네임: {}",
                member.getMemberId(), request.getNickname());

        return ProfileResponse.from(profile);
    }

    @Transactional
    public ProfileResponse updateNickname(Member member, NicknameUpdateRequest request) {

        // 로그 남기기
        log.info("닉네임 수정 요청 - 회원ID: {}, 새닉네임: {}",
                member.getMemberId(), request.getNickname());

        // 해당 회원의 프로필 찾기
        Profile profile = profileRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.error("프로필을 찾을 수 없음 - 회원ID: {}", member.getMemberId());
                    return new RuntimeException("프로필을 찾을 수 없습니다.");
                });

        // 닉네임만 수정하기
        profile.setNickname(request.getNickname());

        log.info("닉네임 수정 성공 - 회원ID: {}, 새닉네임: {}",
                member.getMemberId(), request.getNickname());

        return ProfileResponse.from(profile);
    }
}