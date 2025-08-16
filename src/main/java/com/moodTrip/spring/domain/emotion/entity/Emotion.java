package com.moodTrip.spring.domain.emotion.entity;


import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "M_emotion")
public class Emotion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="tag_id")
    private Integer tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_category_id")
    private EmotionCategory emotionCategory;

    @Column(name = "tag_name")
    private String tagName; // 태그명

    @Column(name = "display_order")
    private Integer displayOrder; // 표시순서


    @Builder.Default
    @OneToMany(mappedBy = "emotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttractionEmotion> attractionEmotions = new ArrayList<>();

}
