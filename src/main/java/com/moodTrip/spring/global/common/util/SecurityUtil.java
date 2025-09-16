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

   // private final ProfileRepository profileRepository;

    /**
     * 🎯 현재 로그인한 회원의 Member 엔티티를 반환
     */
    public Member getCurrentMember() {
        log.debug("🔍 현재 세션에서 사용자 정보 조회 시작");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                log.warn("❌ 인증 정보가 없음");
                throw new RuntimeException("로그인이 필요합니다.");
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof MyUserDetails)) {
                log.warn("❌ Principal이 MyUserDetails가 아님 - principal type: {}",
                        principal != null ? principal.getClass().getSimpleName() : "null");
                throw new RuntimeException("올바르지 않은 인증 정보입니다.");
            }

            MyUserDetails userDetails = (MyUserDetails) principal;
            Member member = userDetails.getMember(); // <-- 세션에 저장된 객체를 그대로 반환

            if (member == null) {
                log.warn("❌ UserDetails에 Member 정보가 없음");
                throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
            }

            if (member.getIsWithdraw() != null && member.getIsWithdraw()) {
                log.warn("❌ 탈퇴한 회원 접근 시도 - memberId: {}", member.getMemberId());
                throw new WithdrawnMemberException("탈퇴하신 회원입니다.");
            }

            // ▼▼▼ 2. 프로필 자동 생성 로직 ★완전 삭제★ ▼▼▼
            /*
            profileRepository.findByMember(member).orElseGet(() -> {
                // ... (이 블록 전체를 삭제) ...
            });
            */

            log.debug("✅ 현재 세션 사용자 조회 성공 - memberId: {}, nickname: {}",
                    member.getMemberId(), member.getNickname());

            // DB 조회 없이 세션의 객체를 그대로 반환합니다.
            // 이 객체는 로그인 시점에 생성된 완전한 객체입니다.
            return member;

        } catch (Exception e) {
            log.error("💥 현재 사용자 정보 조회 중 오류 발생: {}", e.getMessage(), e); // 스택 트레이스 포함
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

    // SecurityUtil.java 클래스 끝 부분에 추가할 메서드

    /**
     * 현재 로그인한 사용자가 관리자인지 확인
     * member_pk가 1인 경우에만 관리자로 인정
     */
    public boolean isAdmin() {
        try {
            Member currentMember = getCurrentMember();
            boolean isAdminUser = currentMember != null && currentMember.getMemberPk().equals(1L);

            log.debug("관리자 권한 체크 - memberId: {}, memberPk: {}, isAdmin: {}",
                    currentMember != null ? currentMember.getMemberId() : "null",
                    currentMember != null ? currentMember.getMemberPk() : "null",
                    isAdminUser);

            return isAdminUser;

        } catch (Exception e) {
            log.warn("관리자 권한 체크 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }




}
