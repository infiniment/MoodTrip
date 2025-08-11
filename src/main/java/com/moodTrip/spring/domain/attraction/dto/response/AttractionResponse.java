package com.moodTrip.spring.domain.attraction.dto.response;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AttractionResponse {
    private Long id;
    private String title;
    private String thumbnail;
    private String tel;
    private String addr1;
    private String addr2;
    private String useTime;
    private String restDate;
    private String parking;
    private String expAgeRange;
    private String overview;
    private List<String> imageUrls;
    private Double mapX;
    private Double mapY;

    public static AttractionResponse from(Attraction entity) {
        return AttractionResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .thumbnail(entity.getThumbnail())
                .tel(entity.getTel())
                .addr1(entity.getAddr1())
                .addr2(entity.getAddr2())
                .useTime(entity.getUseTime())
                .restDate(entity.getRestDate())
                .parking(entity.getParking())
                .expAgeRange(entity.getExpAgeRange())
                .overview(entity.getOverview())
                .mapX(entity.getMapX())
                .mapY(entity.getMapY())
                .build();
    }
}
