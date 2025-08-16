package com.moodTrip.spring.domain.emotion.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
public class AttractionCardDTO {
    private Long id;
    private final String title;
    private final String addr1;
    private final String firstImage;
    private final String description; // 필요 시 추가

    @Builder
    public AttractionCardDTO(Long id,String title, String addr1, String firstImage, String description) {
        this.id = id;
        this.title = title;
        this.addr1 = addr1;
        this.firstImage = firstImage;
        this.description = description;
    }
}