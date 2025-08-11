package com.moodTrip.spring.domain.attraction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Schema(description = "관광지 저장 요청 DTO (detail-page용 필드만)")
public class AttractionInsertRequest {
    private String contentId;
    private String contentTypeId;
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
    private String areaCode;
    private String sigunguCode;
}
