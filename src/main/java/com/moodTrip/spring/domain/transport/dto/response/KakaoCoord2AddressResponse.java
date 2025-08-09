package com.moodTrip.spring.domain.transport.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

// 좌표 → 주소
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoCoord2AddressResponse {
    private List<Document> documents;
    @Getter @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        private RoadAddress road_address;
        private Address address;
        @Getter @JsonIgnoreProperties(ignoreUnknown = true)
        public static class RoadAddress { private String address_name; }
        @Getter @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Address { private String address_name; }
    }
}
