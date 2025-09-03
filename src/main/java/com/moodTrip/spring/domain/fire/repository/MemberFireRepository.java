package com.moodTrip.spring.domain.fire.repository;

import com.moodTrip.spring.domain.fire.entity.MemberFire;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberFireRepository extends JpaRepository<MemberFire, Long> {
    Optional<MemberFire> findByFireReporterAndReportedMemberAndTargetRoom(Member reporter, Member reported, Room room);
    List<MemberFire> findByFireStatus(MemberFire.FireStatus status);
    long countByFireStatus(MemberFire.FireStatus fireStatus);
    // 특정 회원이 신고한 횟수
    long countByFireReporter(Member fireReporter);

    // 특정 회원이 신고받은 횟수
    long countByReportedMember(Member reportedMember);
}
