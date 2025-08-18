package com.moodTrip.spring.domain.emotion.dto.response;


import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AttractionCardDTO {

    @Column(name = "attraction_id")
    private Long attractionId;
    private final String title;
    private final String addr1;
    private final String firstImage;
    private final String description; // 필요 시 추가

    @Builder
    public AttractionCardDTO(Long attractionId,String title, String addr1, String firstImage, String description) {
        this.attractionId = attractionId;
        this.title = title;
        this.addr1 = addr1;
        this.firstImage = firstImage;
        this.description = description;
    }
}