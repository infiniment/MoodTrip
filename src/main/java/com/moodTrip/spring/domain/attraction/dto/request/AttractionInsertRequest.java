package com.moodTrip.spring.domain.attraction.dto.request;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import lombok.Data;

@Data
public class AttractionInsertRequest {
    private Long contentId;
    private Integer contentTypeId;
    private String title;
    private String addr1;
    private String addr2;
    private String zipcode;
    private String tel;
    private String firstImage;
    private String firstImage2;
    private Double mapX;
    private Double mapY;
    private Integer mlevel;
    private Integer areaCode;
    private Integer sigunguCode;

    public Attraction toEntity() {
        return Attraction.builder()
                .contentId(contentId)
                .contentTypeId(contentTypeId)
                .title(title)
                .addr1(addr1)
                .addr2(addr2)
                .zipcode(zipcode)
                .tel(tel)
                .firstImage(firstImage)
                .firstImage2(firstImage2)
                .mapX(mapX)
                .mapY(mapY)
                .mlevel(mlevel)
                .areaCode(areaCode)
                .sigunguCode(sigunguCode)
                .build();
    }
}
