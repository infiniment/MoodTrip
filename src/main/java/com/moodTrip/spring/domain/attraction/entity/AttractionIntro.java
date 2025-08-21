package com.moodTrip.spring.domain.attraction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attraction_intros")
public class AttractionIntro {

    @Id
    private Long contentId;             // attraction.contentId 와 1:1

    private Integer contentTypeId;

    @Column(length = 500)
    private String infocenter;

    @Column(length = 1000)
    private String usetime;

    @Column(length = 1000)
    private String usefee;

    @Column(length = 1000)
    private String parking;

    @Column(length = 1000)
    private String restdate;

    @Column(length = 200)
    private String chkcreditcard;

    @Column(length = 200)
    private String chkbabycarriage;

    @Column(length = 200)
    private String chkpet;

    @Lob
    private String rawJson;             // 원본 JSON 저장(옵션)

    private LocalDateTime syncedAt;
}
