package com.moodTrip.spring.domain.transport.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;


@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoPlaceResponse {
    private List<Document> documents;
    private Meta meta;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        private String place_name;
        private String address_name;
        private String road_address_name;
        private String x; // 경도
        private String y; // 위도
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private int total_count;
        private int pageable_count;
        private boolean is_end;
    }
}