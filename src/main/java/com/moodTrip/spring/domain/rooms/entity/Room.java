package com.moodTrip.spring.domain.rooms.entity;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "room_description", length = 1000)
    private String roomDescription; // 방 설명

    @Column(name = "room_max_count")
    private int roomMaxCount;   // 최대 인원

    @Column(name = "room_current_count")
    private int roomCurrentCount; // 현재 인원


    @Column(name = "travel_start_date")
    private LocalDate travelStartDate; // 여행 시작 날짜

    @Column(name = "travel_end_date")
    private LocalDate travelEndDate; // 여행 종료 날짜

    //    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "attraction_id")
//    private Attraction attraction;
    // 나중에 Attraction 완료되면 교체 예정
    @Column(name = "destination_category")
    private String destinationCategory;

    @Column(name = "destination_name")
    private String destinationName;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmotionRoom> emotionRooms = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private Member creator;

    @Column(name = "is_delete_room")
    private Boolean isDeleteRoom = false;

    // 상우가 방 입장하기 만들 때 추가한 조회수 필드!!
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    // 해당 member가 참여하고 있는 방을 가져오겠다는 뜻
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomMember> roomMembers = new ArrayList<>();

}