package com.moodTrip.spring.domain.attraction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "attraction",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contentId"}))
public class Attraction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TourAPI 고유 ID
    @Column(nullable = false)
    private Long contentId;

    // 12=관광지, 32=숙박 등
    private Integer contentTypeId;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(length = 200) private String addr1;
    @Column(length = 200) private String addr2;
    @Column(length = 20)  private String zipcode;
    @Column(length = 60)  private String tel;

    // 대표 이미지 URL
    private String firstImage;
    private String firstImage2;

    // 좌표
    private Double mapX;   // 경도
    private Double mapY;   // 위도
    private Integer mlevel;

    // 행정 코드
    @Column(nullable = false) private Integer areaCode;   // 광역
    private Integer sigunguCode;                          // 시군구

    // 원문 타임스탬프(yyyyMMddHHmmss → LocalDateTime)
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
}
