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

        if (request.getScheduleTitle() != null) {
            schedule.setScheduleTitle(request.getScheduleTitle());
        }

        if (request.getScheduleDescription() != null) {
            schedule.setScheduleDescription(request.getScheduleDescription());
        }

        if (request.getTravelStartDate() != null) {
            schedule.setTravelStartDate(request.getTravelStartDate());
        }

        schedule.setUpdatedSchedule(LocalDateTime.now());

        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    @Override
    public void deleteSchedule(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    @Override
    public Long getRoomIdByScheduleId(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        return schedule.getRoom().getRoomId();
    }
}
