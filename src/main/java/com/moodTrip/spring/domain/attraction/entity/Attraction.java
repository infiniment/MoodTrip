package com.moodTrip.spring.domain.attraction.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "attraction")
public class Attraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contentId;
    private String contentTypeId;
    private String title;
    private String thumbnail;
    private String tel;
    private String addr1;
    private String addr2;

    @Column(length = 1000)
    private String useTime;

    private String restDate;
    private String parking;
    private String expAgeRange;

    @Column(columnDefinition = "TEXT")
    private String overview;

    private Double mapX;
    private Double mapY;
    private String areaCode;
    private String sigunguCode;
}
