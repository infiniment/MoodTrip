package com.moodTrip.spring.domain.fire.repository;

import com.moodTrip.spring.domain.fire.entity.RoomFire;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomFireRepository extends JpaRepository<RoomFire, Long> {

    // 중복 신고했는지 확인
    Optional<RoomFire> findByFireReporterAndFiredRoom(Member fireReporter, Room firedRoom);
    List<RoomFire> findByFireStatus(RoomFire.FireStatus status);
    long countByFireStatus(RoomFire.FireStatus fireStatus);
}