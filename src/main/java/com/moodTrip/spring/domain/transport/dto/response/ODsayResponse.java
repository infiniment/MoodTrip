package com.moodTrip.spring.domain.transport.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;


@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ODsayResponse {
    private Result result;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private java.util.List<Path> path;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Path {
        private Info info;
        private java.util.List<SubPath> subPath;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Info {
        private Integer totalTime;
        private Integer payment;
        private Integer transferCount;

        private Integer busTransitCount;     // JSON: "busTransitCount"
        private Integer subwayTransitCount;  // JSON: "subwayTransitCount"
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubPath {
        private Integer trafficType;   // 1 지하철 / 2 버스 / 3 도보
        private Integer sectionTime;
        private Integer stationCount;
        private java.util.List<Lane> lane;  // 배열로 받기
        private PassStopList passStopList;
        private String door;           // "null" 문자열 들어올 수 있음
        private String startName;
        private String endName;

        public String getDoor() {
            return (door != null && door.equalsIgnoreCase("null")) ? null : door;
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Lane {
        // 지하철 필드
        private String name;
        private Integer subwayCode;
        private Integer subwayCityCode;

        // 버스 필드
        private String busNo;
        private Integer type;
        private Long busID;
        private String busLocalBlID;
        private Integer busCityCode;
        private Integer busProviderCode;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PassStopList {
        private java.util.List<Station> stations;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Station {
        private Integer index;
        private String stationName;
        private String x;
        private String y;
    }
}
