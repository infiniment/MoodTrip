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

}
