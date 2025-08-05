package com.moodTrip.spring.domain.schedule.entity;

import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private String scheduleTitle;
    private String scheduleDescription;

    private LocalDateTime travelStartDate; // 여행 시작 날짜
    private LocalDateTime travelEndDate; // 여행 종료 날짜

    private LocalDateTime startedSchedule;
    private LocalDateTime updatedSchedule;
}
