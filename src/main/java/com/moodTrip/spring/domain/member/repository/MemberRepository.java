package com.moodTrip.spring.domain.member.repository;

import com.moodTrip.spring.domain.member.entity.Member;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {


    //회원 Id 유효성 검사
    boolean existsByMemberId(String memberId);
    //이메일 유효성 검사
    boolean existsByEmail(String email);

    //아이디 유효성
    Optional<Member> findByMemberId(String memberId);

    // 회원 PK 유효성 검사
    Optional<Member> findByMemberPk(Long memberPk);

    //기존 회원인지(소셜)
    boolean existsByProviderAndProviderId(String provider, String providerId);

    //엔티티를 반환하는 조회 메서드(소셜)
    Optional<Member> findByProviderAndProviderId(String provider, String providerId);

    //비밀번호 찾기 할 때 유효 이메일 인증
    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    // 상우가 추가
    // ========== 🔥 재가입 지원용 새 메서드들 (최소한만 추가) ==========

    /**
     * 활성 상태 회원의 아이디 중복 체크
     * - 회원가입 시 사용 (탈퇴한 회원은 중복 허용)
     */
    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.memberId = :memberId AND m.isWithdraw = false")
    boolean existsByMemberIdAndIsWithdrawFalse(@Param("memberId") String memberId);

    /**
     * 활성 상태 회원의 이메일 중복 체크
     * - 회원가입 시 사용 (탈퇴한 회원은 중복 허용)
     */
    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.email = :email AND m.isWithdraw = false")
    boolean existsByEmailAndIsWithdrawFalse(@Param("email") String email);

    /**
     * 탈퇴한 상태의 회원을 아이디로 찾기
     * - 재가입 시 기존 계정 복구용
     */
    @Query("SELECT m FROM Member m WHERE m.memberId = :memberId AND m.isWithdraw = true")
    Optional<Member> findByMemberIdAndIsWithdrawTrue(@Param("memberId") String memberId);

    /**
     * 탈퇴한 상태의 회원이 해당 아이디로 있는지 확인
     * - 재가입 가능 여부 판단용
     */
    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.memberId = :memberId AND m.isWithdraw = true")
    boolean existsByMemberIdAndIsWithdrawTrue(@Param("memberId") String memberId);

    // MemberRepository.java에 추가
    /**
     * 탈퇴한 소셜 계정 조회
     */
    @Query("SELECT m FROM Member m WHERE m.provider = :provider AND m.providerId = :providerId AND m.isWithdraw = true")
    Optional<Member> findByProviderAndProviderIdAndIsWithdrawTrue(@Param("provider") String provider,
                                                                  @Param("providerId") String providerId);

    //수연
    // 관리자용 전체 회원 목록 (생성일 역순)
    List<Member> findAllByOrderByCreatedAtDesc();

    // 관리자용 회원 검색 (회원ID, 닉네임, 이메일)
    List<Member> findByMemberIdContainingOrNicknameContainingOrEmailContaining(
            String memberId, String nickname, String email);

    //상태별 회원 조회
    List<Member> findByStatus(Member.MemberStatus status);

    //탈퇴하지 않은 활성 회원만 조회
    List<Member> findByIsWithdrawFalseOrderByCreatedAtDesc();

    //신고 받은 횟수가 특정 수 이상인 회원 조회
    List<Member> findByRptRcvdCntGreaterThanEqual(Long count);

    /**
     * 탈퇴한 소셜 계정 존재 여부 확인
     */
    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.provider = :provider AND m.providerId = :providerId AND m.isWithdraw = true")
    boolean existsByProviderAndProviderIdAndIsWithdrawTrue(@Param("provider") String provider,
                                                           @Param("providerId") String providerId);

}