package com.moodTrip.spring.domain.enteringRoom.entity;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "entering_room", uniqueConstraints = {
        @UniqueConstraint(name = "unique_member_room_entering", columnNames = {"member_pk", "room_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnteringRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entering_room_id")
    private Long enteringRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_pk", nullable = false)
    private Member applicant;  // 신청자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;  // 신청한 방

    @Column(name = "message", length = 500, nullable = false)
    private String message;  // 신청 메시지

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EnteringStatus status = EnteringStatus.PENDING;  // 기본값: 대기중

    // 방 입장 상태 열거형
    public enum EnteringStatus {
        PENDING,    // 대기중 (방장이 아직 확인 안함)
        APPROVED,   // 승인됨 (방장이 승인함)
        REJECTED    // 거절됨 (방장이 거절함)
    }
}