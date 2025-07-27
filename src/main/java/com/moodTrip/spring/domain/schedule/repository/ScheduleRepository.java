package com.moodTrip.spring.domain.schedule.repository;

import com.moodTrip.spring.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // roomId 기준으로 조회
    List<Schedule> findByRoomId(Long roomId);

    // 여행 시작일 기준으로 정렬된 스케줄 조회
    List<Schedule> findByRoomIdOrderByTravelStartDateAsc(Long roomId);
}
