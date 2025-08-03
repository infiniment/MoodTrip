package com.moodTrip.spring.domain.emotion.entity;

import com.moodTrip.spring.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.*;

public class AttractionEmotionTag  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long mappingId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "attraction_id")
//    private Attraction attraction; // 관광지 ID (외래키)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Emotion emotion; // 감정 태그(감정) ID (외래키)

    @Column(name = "weight", precision = 5, scale = 3, nullable = false)
    private BigDecimal weight; // 가중치(DECIMAL)


}


