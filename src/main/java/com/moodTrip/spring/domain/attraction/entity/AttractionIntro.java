package com.moodTrip.spring.domain.attraction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attraction_intro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttractionIntro {

    // PK: detailIntro2가 contentId 기준이라 그대로 PK 사용
    @Id
    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "content_type_id")
    private Integer contentTypeId;

    // ===== 주요 필드 =====
    @Column(name = "infocenter")    private String infocenter;   // 문의처
    @Column(name = "usetime")       private String usetime;      // 이용시간
    @Column(name = "usefee")        private String usefee;       // 이용요금
    @Column(name = "parking")       private String parking;      // 주차
    @Column(name = "restdate")      private String restdate;     // 휴일

    // 체험 가능 연령
    @Column(name = "expagerange")   private String expagerange;
    @Column(name = "agelimit")      private String agelimit;
    // 관광지 개요 (detailCommon2에서 받아와 저장)
    @Column(columnDefinition = "TEXT")
    private String overview;

    //장애편의시설
    @Column(name = "a11y_parking")      private String a11yParking;
    @Column(name = "wheelchair")        private String wheelchair;
    @Column(name = "elevator")          private String elevator;
    @Column(name = "braileblock")       private String braileblock;
    @Column(name = "exit_access")       private String exit;              // exit는 예약어 우려 시 컬럼명 별칭
    @Column(name = "guidesystem")       private String guidesystem;
    @Column(name = "signguide")         private String signguide;
    @Column(name = "videoguide")        private String videoguide;
    @Column(name = "audioguide")        private String audioguide;
    @Column(name = "bigprint")          private String bigprint;
    @Column(name = "brailepromotion")   private String brailepromotion;
    @Column(name = "helpdog")           private String helpdog;
    @Column(name = "hearingroom")       private String hearingroom;
    @Column(name = "hearinghandicapetc")private String hearinghandicapetc;
    @Column(name = "blindhandicapetc")  private String blindhandicapetc;
    @Column(name = "handicapetc")       private String handicapetc;
    @Column(name = "publictransport")   private String publictransport;
    @Column(name = "ticketoffice")      private String ticketoffice;
    @Column(name = "guidehuman")        private String guidehuman;

    // ===== 원본/동기화 메타 =====
    @Lob
    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawJson;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;



}