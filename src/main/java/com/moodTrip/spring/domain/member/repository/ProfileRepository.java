package com.moodTrip.spring.domain.member.repository;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    // 회원으로 프로필 찾기
    Optional<Profile> findByMember(Member member);
}
