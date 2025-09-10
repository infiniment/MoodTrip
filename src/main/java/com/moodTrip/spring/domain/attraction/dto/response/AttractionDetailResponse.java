package com.moodTrip.spring.domain.attraction.dto.response;// package는 프로젝트 경로에 맞춰 유지
import com.moodTrip.spring.domain.attraction.entity.AttractionIntro;
import lombok.*;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
public class AttractionDetailResponse {
    private Long attractionId; // 내부 PK
    private Long contentId;    // TourAPI contentId

    private String title;      // 장소명
    private String image;      // 대표 이미지 (firstImage || firstImage2)
    private String tel;        // 문의/안내 (common.tel || intro.infocenter || base.tel)
    private String addr;       // 주소 (common.addrDisplay || addr1 + addr2)
    private String useTime;    // 이용시간
    private String restDate;   // 휴일
    private String parking;    // 주차
    private String age;        // 체험가능 연령
    private String overview;   // 개요(detailCommon2)

    //장애 편의 시설
    private String wheelchair;
    private String elevator;
    private String braileblock;
    private String exit;
    private String guidesystem;
    private String signguide;
    private String videoguide;
    private String audioguide;
    private String bigprint;
    private String brailepromotion;
    private String helpdog;
    private String infantsfamilyetc;
    private String hearingroom;
    private String hearinghandicapetc;
    private String blindhandicapetc;
    private String handicapetc;

    private Double lat; // mapY
    private Double lon; // mapX

    // 내부에서만 쓰는 축약 DTO (서비스 헬퍼와만 연동)
    @Getter @Builder
    public static class DetailCommon {
        private String overview;
        private String infocenter;
        private String usetime;
        private String restdate;
        private String parking;
    }

    @Getter @Builder
    public static class IntroNormalized {
        private String infocenter;
        private String usetime;
        private String restdate;
        private String parking;
        private String age;

        private String wheelchair;
        private String elevator;
        private String braileblock;
        private String exit;
        private String guidesystem;
        private String signguide;
        private String videoguide;
        private String audioguide;
        private String bigprint;
        private String brailepromotion;
        private String helpdog;
        private String infantsfamilyetc;
        private String hearingroom;
        private String hearinghandicapetc;
        private String blindhandicapetc;
        private String handicapetc;

    }

    public static AttractionDetailResponse of(
            AttractionResponse base,
            AttractionDetailResponse.IntroNormalized intro,
            AttractionDetailResponse.DetailCommon common,
            AttractionIntro a11y
    ) {
        String image = firstNonBlank(base.getFirstImage(), base.getFirstImage2());
        String tel = firstNonBlank( intro.getInfocenter(), base.getTel());
        String addr = firstNonBlank(
                joinNonBlank(" ", base.getAddr1(), base.getAddr2()));

        return AttractionDetailResponse.builder()
                .contentId(base.getContentId())
                .title(base.getTitle())
                .image(image)
                .tel(tel)
                .addr(addr)
                .useTime(firstNonBlank(intro.getUsetime()))
                .restDate(firstNonBlank(intro.getRestdate()))
                .parking(firstNonBlank(intro.getParking()))
                .age(firstNonBlank(intro.getAge()))
                .overview(common != null ? common.getOverview() : null)
                .attractionId(base.getAttractionId())
                .wheelchair(a11y.getWheelchair())
                .elevator(a11y.getElevator())
                .braileblock(a11y.getBraileblock())
                .exit(a11y.getExit())
                .guidesystem(a11y.getGuidesystem())
                .signguide(a11y.getSignguide())
                .videoguide(a11y.getVideoguide())
                .audioguide(a11y.getAudioguide())
                .bigprint(a11y.getBigprint())
                .brailepromotion(a11y.getBrailepromotion())
                .helpdog(a11y.getHelpdog())
                .infantsfamilyetc(a11y.getInfantsfamilyetc())
                .hearingroom(a11y.getHearingroom())
                .hearinghandicapetc(a11y.getHearinghandicapetc())
                .blindhandicapetc(a11y.getBlindhandicapetc())
                .handicapetc(a11y.getHandicapetc())
                .attractionId(base.getAttractionId())
                .lat(base.getMapY()) // 위도
                .lon(base.getMapX()) // 경도
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
