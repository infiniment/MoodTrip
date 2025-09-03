package com.moodTrip.spring.domain.admin.service;

import com.moodTrip.spring.domain.admin.dto.response.AdminMatchingDto;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.schedule.entity.Schedule;
import com.moodTrip.spring.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMatchingService {

    private final RoomRepository roomRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public List<AdminMatchingDto> getAllMatchings() {
        // ✅ 이제 그냥 findAll() 호출해도 creator/attraction 같이 fetch 됨
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // 페이징 조회
    @Transactional(readOnly = true)
    public Page<AdminMatchingDto> getMatchings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        // ✅ 페이징도 그대로 findAll(pageable) 호출
        Page<Room> rooms = roomRepository.findAll(pageable);
        return rooms.map(this::mapToDto);
    }

    private AdminMatchingDto mapToDto(Room room) {
        int period = 0;
        if (room.getTravelStartDate() != null && room.getTravelEndDate() != null) {
            period = (int) ChronoUnit.DAYS.between(room.getTravelStartDate(), room.getTravelEndDate()) + 1;
        }

        return AdminMatchingDto.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .creatorNickname(room.getCreator() != null ? room.getCreator().getNickname() : "-")
                .currentCount(room.getRoomCurrentCount())
                .maxCount(room.getRoomMaxCount())
                .travelStartDate(room.getTravelStartDate())
                .travelEndDate(room.getTravelEndDate())
                .travelPeriod(period)
                .regionName(room.getAttraction() != null
                        ? com.moodTrip.spring.global.util.AreaCodeMapper.getAreaName(room.getAttraction().getAreaCode())
                        : "-")
                .attractionTitle(room.getAttraction() != null
                        ? room.getAttraction().getTitle()
                        : "-")
                .status(determineStatus(room.getTravelEndDate()))
                .createdAt(room.getCreatedAt())  // 생성일 추가
                .participants(room.getRoomMembers() != null ?
                        room.getRoomMembers().stream()
                                .filter(rm -> Boolean.TRUE.equals(rm.getIsActive()))
                                .map(rm -> rm.getMember().getNickname())
                                .collect(Collectors.toList()) :
                        List.of())
                .build();
    }

    private String getStatus(Room room) {
        if (Boolean.TRUE.equals(room.getIsDeleteRoom())) {
            return "취소";
        }
        if (room.getTravelEndDate() != null && room.getTravelEndDate().isBefore(LocalDate.now())) {
            return "완료";
        }
        return "진행중";
    }

    private String toStatusClass(String status) {
        return switch (status) {
            case "진행중" -> "active";
            case "완료" -> "completed";
            case "취소" -> "cancelled";
            default -> "active";
        };
    }

    private String determineStatus(LocalDate travelEndDate) {
        if (travelEndDate == null) return "진행중";
        LocalDate today = LocalDate.now();
        if (travelEndDate.isBefore(today)) {
            return "완료";
        }
        return "진행중";
    }

    @Transactional
    public void terminateMatching(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방이 존재하지 않습니다."));

        // 스케줄 삭제
        List<Schedule> schedules = scheduleRepository.findByRoom(room);
        if (!schedules.isEmpty()) {
            scheduleRepository.deleteAll(schedules);
        }

        room.setIsDeleteRoom(true);
        roomRepository.save(room);
    }

    @Transactional
    public void restoreMatching(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 방이 존재하지 않습니다."));

        room.setIsDeleteRoom(false);
        roomRepository.save(room);
    }

}
