package com.moodTrip.spring.domain.emotion.entity;

import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList; // ArrayList import 추가
import java.util.List;      // List import 추가
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "emotion_category")
public class EmotionCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emotion_category_id")
    private Long emotionCategoryId; // 감정카테고리ID(PK)

    @Column(name = "emotion_category_name")
    private String emotionCategoryName; // 감정 카테고리명

    @Column(name = "emotion_category_icon")
    private String emotionCategoryIcon; // 아이콘

    @Column(name = "display_order")
    private Integer displayOrder; // 표시순서

    // Emotion 엔터티와의 관계를 정의하는 List 필드를 추가합니다.

    @OneToMany(mappedBy = "emotionCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Emotion> emotions = new ArrayList<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmotionCategory that = (EmotionCategory) o;
        return Objects.equals(emotionCategoryId, that.emotionCategoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emotionCategoryId);
    }

}
