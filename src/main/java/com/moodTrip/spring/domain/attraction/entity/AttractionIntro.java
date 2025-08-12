package com.moodTrip.spring.domain.attraction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attraction_intro")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AttractionIntro {

    /** TourAPI contentId 를 그대로 PK로 사용 */
    @Id
    private Long contentId;

    private Integer contentTypeId;

    // 자주 쓰는 소개 필드(비어있을 수 있음)
    private String infocenter;       // 문의/안내
    private String usetime;          // 이용시간/운영시간
    private String usefee;           // 이용요금
    private String parking;          // 주차
    private String restdate;         // 휴무일
    private String chkcreditcard;    // 카드 가능
    private String chkbabycarriage;  // 유모차 대여/반입
    private String chkpet;           // 반려동물 가능

    /** 원본 JSON 전체 백업 */
    @Lob
    @Column(columnDefinition = "longtext")
    private String rawJson;

    private LocalDateTime syncedAt;
}
