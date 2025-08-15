package com.moodTrip.spring.domain.member.repository;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    // 회원으로 프로필 찾기
    Optional<Profile> findByMember(Member member);

    // member_pk로 프로필 찾기
    Optional<Profile> findByMember_MemberPk(Long memberPk);
}
