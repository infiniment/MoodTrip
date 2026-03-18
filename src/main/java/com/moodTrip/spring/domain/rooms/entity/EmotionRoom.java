package com.moodTrip.spring.domain.rooms.entity;

import com.moodTrip.spring.domain.emotion.entity.Emotion;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Emotion emotion;

    public void setRoom(Room room) {
        this.room = room;
        if (room != null && !room.getEmotionRooms().contains(this)) {
            room.getEmotionRooms().add(this);
        }
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
        if (emotion != null && !emotion.getEmotionRooms().contains(this)) {
            emotion.getEmotionRooms().add(this);
        }
    }

}
