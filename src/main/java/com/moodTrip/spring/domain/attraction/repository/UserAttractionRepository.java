package com.moodTrip.spring.domain.attraction.repository;
import com.moodTrip.spring.domain.attraction.entity.Attraction;
import com.moodTrip.spring.domain.attraction.entity.UserAttraction;
import com.moodTrip.spring.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

// MMember, Mattraction 엔티티는 이미 존재한다고 가정합니다.
// 파일명은 ERD를 참고하여 UserAttraction.java, Member.java, Attraction.java로 합니다.
public interface UserAttractionRepository extends JpaRepository<UserAttraction, Long> {

    // 회원이 특정 관광지를 이미 찜했는지 확인하기 위한 메서드
    boolean existsByMemberAndAttraction(Member member, Attraction attraction);

    // 찜 취소를 위해 회원과 관광지 정보로 데이터를 찾아 삭제하기 위한 메서드
    void deleteByMemberAndAttraction(Member member, Attraction attraction);

    @Query(value = "SELECT ua FROM UserAttraction ua JOIN FETCH ua.attraction WHERE ua.member.memberPk = :memberPk",
            countQuery = "SELECT count(ua) FROM UserAttraction ua WHERE ua.member.memberPk = :memberPk")
    Page<UserAttraction> findByMemberMemberPkWithAttraction(@Param("memberPk") Long memberPk, Pageable pageable);

    @Query("SELECT ua.attraction.id FROM UserAttraction ua WHERE ua.member = :member")
    Set<Long> findLikedAttractionIdsByMember(@Param("member") Member member);

    @Query("SELECT ua FROM UserAttraction ua JOIN FETCH ua.attraction WHERE ua.member.memberPk = :memberPk")
    List<UserAttraction> findByMemberMemberPkWithAttraction(@Param("memberPk") Long memberPk);



}
