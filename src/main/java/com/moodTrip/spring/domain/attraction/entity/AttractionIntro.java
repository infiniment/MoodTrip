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

    // ===== 공통 표준 필드 (정규화 대상) =====
    @Column(name = "infocenter")        private String infocenter;
    @Column(name = "usetime")           private String usetime;
    @Column(name = "usefee")            private String usefee;
    @Column(name = "parking")           private String parking;
    @Column(name = "restdate")          private String restdate;

    @Column(name = "chkcreditcard")     private String chkcreditcard;
    @Column(name = "chkbabycarriage")   private String chkbabycarriage;
    @Column(name = "chkpet")            private String chkpet;

    // 체험 연령 관련
    @Column(name = "expagerange")           private String expagerange;          // 기본
    @Column(name = "expagerangeleports")    private String expagerangeleports;   // 레포츠
    @Column(name = "agelimit")              private String agelimit;

    // ===== 분기(콘텐츠 유형별) 필드 — 정규화에서 firstNonEmpty(...)로 흡수 =====
    // infocenter*
    @Column(name = "infocenterfood")        private String infocenterfood;
    @Column(name = "infocenterlodging")     private String infocenterlodging;
    @Column(name = "infocenterculture")     private String infocenterculture;
    @Column(name = "infocentershopping")    private String infocentershopping;
    @Column(name = "infocenterleports")     private String infocenterleports;
    @Column(name = "infocentertourcourse")  private String infocentertourcourse;

    // usetime* / opentime*
    @Column(name = "usetimeculture")        private String usetimeculture;
    @Column(name = "usetimefestival")       private String usetimefestival;
    @Column(name = "usetimeleports")        private String usetimeleports;
    @Column(name = "opentime")              private String opentime;
    @Column(name = "opentimefood")          private String opentimefood;

    // restdate*
    @Column(name = "restdatefood")          private String restdatefood;
    @Column(name = "restdateculture")       private String restdateculture;
    @Column(name = "restdateshopping")      private String restdateshopping;
    @Column(name = "restdateleports")       private String restdateleports;

    // parking*
    @Column(name = "parkingfood")           private String parkingfood;
    @Column(name = "parkingculture")        private String parkingculture;
    @Column(name = "parkingshopping")       private String parkingshopping;
    @Column(name = "parkinglodging")        private String parkinglodging;
    @Column(name = "parkingleports")        private String parkingleports;

    // 카드/유모차/반려동물 분기
    @Column(name = "chkcreditcardfood")     private String chkcreditcardfood;
    @Column(name = "chkcreditcardculture")  private String chkcreditcardculture;
    @Column(name = "chkcreditcardshopping") private String chkcreditcardshopping;
    @Column(name = "chkcreditcardleports")  private String chkcreditcardleports;

    @Column(name = "chkbabycarriageshopping") private String chkbabycarriageshopping;
    @Column(name = "chkbabycarriageleports")  private String chkbabycarriageleports;
    @Column(name = "chkbabycarriageculture")  private String chkbabycarriageculture;

    @Column(name = "chkpetculture")        private String chkpetculture;
    @Column(name = "chkpetshopping")       private String chkpetshopping;
    @Column(name = "chkpetleports")        private String chkpetleports;

    // 사용료(레포츠 분기)
    @Column(name = "usefeeleports")        private String usefeeleports;

    // ===== 원본/동기화 메타 =====
    @Lob
    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawJson;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
}
