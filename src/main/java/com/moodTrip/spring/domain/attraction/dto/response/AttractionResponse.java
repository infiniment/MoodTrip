package com.moodTrip.spring.domain.attraction.dto.response;

import com.moodTrip.spring.domain.attraction.entity.Attraction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AttractionResponse {
    private Long id;
    private Long contentId;
    private Integer contentTypeId;
    private String title;
    private String addr1;
    private String addr2;
    private String tel;
    private String firstImage;
    private String firstImage2;
    private Double mapX;
    private Double mapY;
    private Integer areaCode;
    private Integer sigunguCode;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;

    private static String normalizeTel(String tel) {
        if (tel == null) return null;
        // <br>, <br/>, <br /> 모두 처리(대소문자 무시)
        return tel.replaceAll("(?i)<\\s*br\\s*/?>", "\n").trim();
    }

    public static AttractionResponse from(Attraction a) {
        return AttractionResponse.builder()
                .id(a.getAttractionId())
                .contentId(a.getContentId())
                .contentTypeId(a.getContentTypeId())
                .title(a.getTitle())
                .addr1(a.getAddr1())
                .addr2(a.getAddr2())
                .tel(a.getTel())
                .firstImage(a.getFirstImage())
                .firstImage2(a.getFirstImage2())
                .mapX(a.getMapX())
                .mapY(a.getMapY())
                .areaCode(a.getAreaCode())
                .sigunguCode(a.getSigunguCode())
                .createdTime(a.getCreatedTime())
                .modifiedTime(a.getModifiedTime())
                .build();
    }
}

