package com.moodTrip.spring.domain.rooms.entity;

import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "emotion_room")
public class EmotionRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emotionRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "tag_id")
//    private Emotion emotion;
}
