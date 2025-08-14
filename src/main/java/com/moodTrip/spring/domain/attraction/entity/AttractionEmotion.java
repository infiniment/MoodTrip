package com.moodTrip.spring.domain.attraction.entity;

import com.moodTrip.spring.domain.emotion.entity.Emotion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// ERD의 테이블명 및 복합 유니크 키 제약조건 적용
@Table(name = "attraction_emotion_tags",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_attraction_emotion_tags",
                        columnNames = {"attraction_id", "tag_id"}
                )
        })
public class AttractionEmotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long id; // ERD의 mapping_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id", nullable = false)
    private Attraction attraction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Emotion emotion;

    // ERD의 weight 필드 (DECIMAL 타입)
    @Column(name = "weight", precision = 10, scale = 5)
    private BigDecimal weight;

    // ERD의 created_at 필드
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ERD의 is_active 필드
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    private boolean isActive = true;

    // 생성자나 @PrePersist를 사용하여 createdAt 초기화
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }


}