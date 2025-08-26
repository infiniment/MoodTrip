package com.moodTrip.spring.domain.attraction.dto.response;// package는 프로젝트 경로에 맞춰 유지
import lombok.*;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
public class AttractionDetailResponse {

    private Long attractionId;

    private String title;      // 장소명
    private String image;      // 대표 이미지 (firstImage || firstImage2)
    private String tel;        // 문의/안내 (common.tel || intro.infocenter || base.tel)
    private String addr;       // 주소 (common.addrDisplay || addr1 + addr2)
    private String useTime;    // 이용시간
    private String restDate;   // 휴일
    private String parking;    // 주차
    private String age;        // 체험가능 연령
    private String overview;   // 개요(detailCommon2)



    // 내부에서만 쓰는 축약 DTO (서비스 헬퍼와만 연동)
    @Getter @Builder
    public static class DetailCommon {
        private String tel;
        private String overview;
        private String addrDisplay;
    }

    @Getter @Builder
    public static class IntroNormalized {
        private String infocenter;
        private String usetime;
        private String restdate;
        private String parking;
        private String age;
    }

    // 통합 생성 (API 값만 사용)
    public static AttractionDetailResponse of(
            com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse base,
            IntroNormalized intro,
            DetailCommon common
    ) {
        String image = firstNonBlank(base.getFirstImage(), base.getFirstImage2());
        String tel   = firstNonBlank(common.getTel(), intro.getInfocenter(), base.getTel());
        String addr  = firstNonBlank(common.getAddrDisplay(),
                joinNonBlank(" ", base.getAddr1(), base.getAddr2()));

        return AttractionDetailResponse.builder()


                .title(base.getTitle())
                .image(image)
                .tel(tel)
                .addr(addr)
                .useTime(firstNonBlank(intro.getUsetime()))
                .restDate(firstNonBlank(intro.getRestdate()))
                .parking(firstNonBlank(intro.getParking()))
                .age(firstNonBlank(intro.getAge()))
                .overview(common.getOverview())
                .attractionId(base.getAttractionId())
                .build();
    }

    private static String firstNonBlank(String... v) {
        if (v == null) return null;
        for (String s : v) if (s != null && !s.isBlank()) return s;
        return null;
    }
    private static String joinNonBlank(String sep, String... v) {
        return java.util.Arrays.stream(v)
                .filter(s -> s != null && !s.isBlank())
                .reduce((a,b) -> a + sep + b)
                .orElse(null);
    }
}
