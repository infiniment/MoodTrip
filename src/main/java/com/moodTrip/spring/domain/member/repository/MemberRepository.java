package com.moodTrip.spring.domain.member.repository;

import com.moodTrip.spring.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

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


}
