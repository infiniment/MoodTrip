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
                .nickname(member.getNickname())
                .email(member.getEmail())
                .memberPhone(member.getMemberPhone())
                .profileBio("안녕하세요! 여행을 좋아하는 " + member.getNickname() + "입니다. 함께 즐거운 여행 떠나요! 🌍✈️")
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
        memberToUpdate.setEmail(request.getNickname());

        // 3. Profile 정보 수정
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

        // ✅ 유효성 검사 추가 (백엔드 보안)
        String newNickname = request.getNickname();
        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new RuntimeException("닉네임은 필수 입력 항목입니다.");
        }

        newNickname = newNickname.trim();

        if (newNickname.length() > 30) {
            throw new RuntimeException("닉네임은 30자 이내로 입력해주세요.");
        }

        // 한글, 영문, 숫자만 허용
        if (!newNickname.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new RuntimeException("닉네임은 한글, 영문, 숫자만 사용 가능합니다.");
        }

        // 해당 회원의 프로필 찾기
        Profile profile = profileRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.error("프로필을 찾을 수 없음 - 회원ID: {}", member.getMemberId());
                    return new RuntimeException("프로필을 찾을 수 없습니다.");
                });

        // ✅ Member 엔티티의 memberName을 닉네임으로 직접 수정
        Member memberToUpdate = profile.getMember();
        memberToUpdate.setNickname(newNickname);  // 🔥 핵심 변경점!

        // ✅ Profile의 nickname 필드도 제거했다면 이 줄은 삭제
        // profile.setNickname(request.getNickname()); // 삭제

        log.info("닉네임 수정 성공 - 회원ID: {}, 새닉네임: {}",
                member.getMemberId(), newNickname);

        return ProfileResponse.from(profile);
    }
}