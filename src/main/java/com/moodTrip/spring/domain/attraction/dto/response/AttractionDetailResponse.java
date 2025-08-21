package com.moodTrip.spring.domain.attraction.dto.response;

import com.moodTrip.spring.domain.attraction.entity.AttractionIntro;
import lombok.Builder;

// dto/response/AttractionDetailDto.java
@Builder
public record AttractionDetailResponse(
        Long contentId, Integer contentTypeId,
        String title, String addr1, String addr2, String tel,
        String firstImage, String firstImage2,
        String infocenter, String usetime, String usefee,
        String parking, String restdate, String chkcreditcard,
        String chkbabycarriage, String chkpet
){
    public static AttractionDetailResponse of(AttractionResponse b, AttractionIntro i) {
        return AttractionDetailResponse.builder()
                .contentId(b.getContentId())
                .contentTypeId(b.getContentTypeId())
                .title(b.getTitle())
                .addr1(b.getAddr1()).addr2(b.getAddr2()).tel(b.getTel())
                .firstImage(b.getFirstImage()).firstImage2(b.getFirstImage2())
                .infocenter(i == null ? null : i.getInfocenter())
                .usetime(i == null ? null : i.getUsetime())
                .usefee(i == null ? null : i.getUsefee())
                .parking(i == null ? null : i.getParking())
                .restdate(i == null ? null : i.getRestdate())
                .chkcreditcard(i == null ? null : i.getChkcreditcard())
                .chkbabycarriage(i == null ? null : i.getChkbabycarriage())
                .chkpet(i == null ? null : i.getChkpet())
                .build();
    }
}
