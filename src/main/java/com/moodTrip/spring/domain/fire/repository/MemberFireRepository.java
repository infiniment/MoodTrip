package com.moodTrip.spring.domain.fire.repository;

import com.moodTrip.spring.domain.fire.entity.MemberFire;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberFireRepository extends JpaRepository<MemberFire, Long> {
    Optional<MemberFire> findByFireReporterAndReportedMemberAndTargetRoom(Member reporter, Member reported, Room room);
}
