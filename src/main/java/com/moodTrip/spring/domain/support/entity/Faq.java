package com.moodTrip.spring.domain.support.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;     // 예: "서비스 소개", "결제"
    private String title;

    @Lob
    private String content;

    private int views;

    private int helpful;
    private int notHelpful;
}
