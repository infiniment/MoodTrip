package com.moodTrip.spring.domain.support.entity;

import com.moodTrip.spring.domain.admin.entity.Notification;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_helpful",
        uniqueConstraints = @UniqueConstraint(columnNames = {"notification_id","member_pk"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationHelpful extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_pk", nullable = false)
    private Member member;
}