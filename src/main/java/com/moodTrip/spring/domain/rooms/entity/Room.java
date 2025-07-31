package com.moodTrip.spring.domain.rooms.entity;

import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "companion_room")
public class Room extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    private String roomName; // 방 이름

    @Column(length = 1000)
    private String roomDescription; // 방 설명
    private int roomMaxCount;   // 최대 인원
    private int roomCurrentCount; // 현재 인원

    private LocalDate travelStartDate; // 여행 시작 날짜
    private LocalDate travelEndDate; // 여행 종료 날짜

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "attraction_id")
//    private Attraction attraction;
//
//    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<EmotionRoom> emotionRooms = new ArrayList<>();
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "creator_id")
//    private Member creator;

    @Column(name = "is_delete_room")
    private Boolean isDeleteRoom = false;



}
