package com.moodTrip.spring.global.common.util;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import com.moodTrip.spring.global.common.exception.WithdrawnMemberException;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final ProfileRepository profileRepository;

    /**
     * 🎯 현재 로그인한 회원의 Member 엔티티를 반환
     */
    public Member getCurrentMember() {
        log.debug("🔍 현재 로그인한 사용자 정보 조회 시작");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("❌ 인증 정보가 없음 - 로그인이 필요합니다");
                throw new RuntimeException("로그인이 필요합니다.");
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof MyUserDetails)) {
                log.warn("❌ Principal이 MyUserDetails가 아님 - principal type: {}",
                        principal != null ? principal.getClass().getSimpleName() : "null");
                throw new RuntimeException("올바르지 않은 인증 정보입니다.");
            }

            MyUserDetails userDetails = (MyUserDetails) principal;
            Member member = userDetails.getMember();

            if (member == null) {
                log.warn("❌ UserDetails에 Member 정보가 없음");
                throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
            }

            if (member.getIsWithdraw() != null && member.getIsWithdraw()) {
                log.warn("❌ 탈퇴한 회원 접근 시도 - memberId: {}", member.getMemberId());
                throw new WithdrawnMemberException("탈퇴하신 회원입니다.");
            }

            // ✅ 프로필 자동 생성 로직
            profileRepository.findByMember(member).orElseGet(() -> {
                log.info("🌱 프로필이 존재하지 않아 새로 생성합니다 - memberId: {}", member.getMemberId());
                Profile newProfile = Profile.builder()
                        .member(member)
                        .profileImage("/image/fix/moodtrip.png")
                        .profileBio("반갑습니다")
                        .build();
                return profileRepository.save(newProfile);
            });

            log.debug("✅ 현재 로그인한 사용자 조회 성공 - memberId: {}, nickname: {}",
                    member.getMemberId(), member.getNickname());

            return member;

        } catch (Exception e) {
            log.error("💥 현재 사용자 정보 조회 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("사용자 인증 정보를 가져올 수 없습니다: " + e.getMessage());
        }
    }

    public String getCurrentMemberId() {
        Member currentMember = getCurrentMember();
        return currentMember.getMemberId();
    }

    public Long getCurrentMemberPk() {
        Member currentMember = getCurrentMember();
        return currentMember.getMemberPk();
    }

    public String getCurrentNickname() {
        Member currentMember = getCurrentMember();
        return currentMember.getNickname();
    }

    public boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof MyUserDetails;
        } catch (Exception e) {
            log.debug("인증 상태 확인 중 오류: {}", e.getMessage());
            return false;
        }
    }

    public boolean isCurrentMember(Long memberPk) {
        try {
            Long currentMemberPk = getCurrentMemberPk();
            return currentMemberPk.equals(memberPk);
        } catch (Exception e) {
            return false;
        }
    }




}
