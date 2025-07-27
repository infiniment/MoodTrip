package com.moodTrip.spring.domain.schedule.entity;

import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
    private String scheduleTitle;
    private String scheduleDescription;
    private LocalDateTime travelStartDate; // 스케줄 시작 시간(=날짜 + 시각)
}
