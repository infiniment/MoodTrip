package com.moodTrip.spring.domain.rooms.entity;

import com.moodTrip.spring.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room_members", uniqueConstraints = {
        @UniqueConstraint(name = "unique_room_members", columnNames = {"member_pk", "room_id"})
})
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_room_id")
    private Long memberRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_pk")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "role")
    private String role; // 예: LEADER, MEMBER 등

    @Column(name = "is_active")
    private Boolean isActive;

}
