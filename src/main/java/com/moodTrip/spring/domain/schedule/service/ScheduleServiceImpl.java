package com.moodTrip.spring.domain.schedule.service;

import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.schedule.dto.request.ScheduleRequest;
import com.moodTrip.spring.domain.schedule.dto.response.ScheduleResponse;
import com.moodTrip.spring.domain.schedule.entity.Schedule;
import com.moodTrip.spring.domain.schedule.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final RoomRepository roomRepository;


    @Override
    public List<ScheduleResponse> getSchedulesByRoomId(Long roomId) {
        return scheduleRepository.findByRoom_RoomId(roomId)
                .stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @Override
    public ScheduleResponse createSchedule(Long roomId, ScheduleRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        Schedule schedule = Schedule.builder()
                .room(room)
                .scheduleTitle(request.getScheduleTitle())
                .scheduleDescription(request.getScheduleDescription())
                .travelStartDate(request.getTravelStartDate())
                .startedSchedule(LocalDateTime.now())
                .updatedSchedule(LocalDateTime.now())
                .build();

        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    @Override
    public ScheduleResponse updateSchedule(Long scheduleId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정이 존재하지 않습니다."));

        schedule.setScheduleTitle(request.getScheduleTitle());
        schedule.setScheduleDescription(request.getScheduleDescription());
        schedule.setTravelStartDate(request.getTravelStartDate());
        schedule.setUpdatedSchedule(LocalDateTime.now());

        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    @Override
    public void deleteSchedule(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }
}
