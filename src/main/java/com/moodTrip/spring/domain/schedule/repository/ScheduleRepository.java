package com.moodTrip.spring.domain.schedule.repository;

import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // roomId 기준으로 조회
    List<Schedule> findByRoom_RoomId(Long roomId);

    // 여행 시작일 기준 정렬
    List<Schedule> findByRoom_RoomIdOrderByTravelStartDateAsc(Long roomId);
}
