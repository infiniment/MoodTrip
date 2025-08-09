package com.moodTrip.spring.domain.transport.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

// 주소 → 좌표
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoAddressSearchResponse {
    private List<Document> documents;
    @Getter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        private RoadAddress road_address;
        private Address address;
        @Getter @JsonIgnoreProperties(ignoreUnknown = true)
        public static class RoadAddress { private String address_name; private String x; private String y; }
        @Getter @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Address { private String address_name; private String x; private String y; }
    }
}

