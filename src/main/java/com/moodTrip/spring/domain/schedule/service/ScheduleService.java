package com.moodTrip.spring.domain.schedule.service;

import com.moodTrip.spring.domain.schedule.dto.request.ScheduleRequest;
import com.moodTrip.spring.domain.schedule.entity.Schedule;
import com.moodTrip.spring.domain.schedule.repository.ScheduleRepository;
import com.moodTrip.spring.global.common.code.status.ErrorStatus;
import com.moodTrip.spring.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final RoomRepository roomRepository;

    public Schedule addSchedule(Long roomId, ScheduleRequest request) {
        // roomId로 Room 조회(없으면 예외처리)
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorStatus.ROOM_NOT_FOUND));
        // Schedule 엔티티 생성
        Schedule schedule = Schedule.builder()
                .room(room)
                .scheduleTitle(request.getTitle())
                .scheduleDescription(request.getDescription())
                .travelStartDate(request.getDate().atTime(request.getTime()))
                .build();
        // 저장 후 반환
        return scheduleRepository.save(schedule);
    }
}
