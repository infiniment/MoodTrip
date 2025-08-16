package com.moodTrip.spring.domain.fire.repository;

import com.moodTrip.spring.domain.fire.entity.Fire;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FireRepository extends JpaRepository<Fire, Long> {

    // 중복 신고했는지 확인
    Optional<Fire> findByFireReporterAndFiredRoom(Member fireReporter, Room firedRoom);

}