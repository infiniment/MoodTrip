package com.moodTrip.spring.global.common.util;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 🔐 현재 로그인한 사용자 정보를 추출하는 유틸리티 클래스
 *
 * 왜 이 클래스가 필요한가요?
 * - 더미 데이터(createTestMember) 대신 실제 JWT 인증 사용자 정보 사용
 * - 모든 컨트롤러에서 "현재 로그인한 사용자"를 쉽게 가져오기 위해
 * - JWT 토큰 처리 로직을 한 곳에 모아서 유지보수성 향상
 * - 중복 코드 제거
 *
 * 팀원분의 JWT 구조 활용:
 * 1. JwtAuthenticationFilter가 쿠키에서 JWT 토큰 추출
 * 2. JwtUtil로 토큰에서 memberId 추출
 * 3. CustomUserDetailsService가 memberId로 Member 조회
 * 4. MyUserDetails(Member 포함)가 SecurityContext에 저장
 * 5. 이 클래스에서 SecurityContext → MyUserDetails → Member 순으로 추출
 */
@Slf4j
@Component  // ✅ Spring이 관리하는 빈으로 등록
public class SecurityUtil {

    /**
     * 🎯 현재 로그인한 회원의 Member 엔티티를 반환
     *
     * 동작 원리:
     * 1. SecurityContext에서 Authentication 객체 가져오기
     * 2. Authentication의 Principal을 MyUserDetails로 캐스팅
     * 3. MyUserDetails에서 Member 엔티티 추출
     *
     * @return 현재 로그인한 회원의 Member 엔티티
     * @throws RuntimeException 로그인하지 않았거나 인증 정보가 없는 경우
     */
    public Member getCurrentMember() {
        log.debug("🔍 현재 로그인한 사용자 정보 조회 시작");

        try {
            // 1️⃣ SecurityContext에서 인증 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 2️⃣ 인증 정보가 없거나 인증되지 않은 경우 체크
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("❌ 인증 정보가 없음 - 로그인이 필요합니다");
                throw new RuntimeException("로그인이 필요합니다.");
            }

            // 3️⃣ Principal이 MyUserDetails인지 확인
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof MyUserDetails)) {
                log.warn("❌ Principal이 MyUserDetails가 아님 - principal type: {}",
                        principal != null ? principal.getClass().getSimpleName() : "null");
                throw new RuntimeException("올바르지 않은 인증 정보입니다.");
            }

            // 4️⃣ MyUserDetails에서 Member 엔티티 추출
            MyUserDetails userDetails = (MyUserDetails) principal;
            Member member = userDetails.getMember();

            if (member == null) {
                log.warn("❌ UserDetails에 Member 정보가 없음");
                throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
            }

            // 5️⃣ 탈퇴한 회원인지 체크
            if (member.getIsWithdraw() != null && member.getIsWithdraw()) {
                log.warn("❌ 탈퇴한 회원 접근 시도 - memberId: {}", member.getMemberId());
                throw new RuntimeException("탈퇴한 회원입니다.");
            }

            log.debug("✅ 현재 로그인한 사용자 조회 성공 - memberId: {}, nickname: {}",
                    member.getMemberId(), member.getNickname());

            return member;

        } catch (Exception e) {
            log.error("💥 현재 사용자 정보 조회 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("사용자 인증 정보를 가져올 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 🔍 현재 로그인한 회원의 ID(memberId)를 반환
     *
     * @return 현재 로그인한 회원의 memberId (예: "testuser123", "kakao_12345")
     */
    public String getCurrentMemberId() {
        Member currentMember = getCurrentMember();
        return currentMember.getMemberId();
    }

    /**
     * 🔍 현재 로그인한 회원의 PK를 반환
     *
     * @return 현재 로그인한 회원의 memberPk (예: 1L, 2L)
     */
    public Long getCurrentMemberPk() {
        Member currentMember = getCurrentMember();
        return currentMember.getMemberPk();
    }

    /**
     * 🔍 현재 로그인한 회원의 닉네임을 반환
     *
     * @return 현재 로그인한 회원의 nickname
     */
    public String getCurrentNickname() {
        Member currentMember = getCurrentMember();
        return currentMember.getNickname();
    }

    /**
     * 🔍 현재 사용자가 로그인했는지 확인
     *
     * @return 로그인 상태면 true, 아니면 false
     */
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

    /**
     * 🔍 현재 로그인한 회원이 특정 회원과 같은지 확인
     *
     * @param memberPk 비교할 회원의 PK
     * @return 같은 회원이면 true, 아니면 false
     */
    public boolean isCurrentMember(Long memberPk) {
        try {
            Long currentMemberPk = getCurrentMemberPk();
            return currentMemberPk.equals(memberPk);
        } catch (Exception e) {
            return false;
        }
    }
}