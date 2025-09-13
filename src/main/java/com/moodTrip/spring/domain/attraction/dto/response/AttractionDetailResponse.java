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
    private String a11yParking;
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
    private String hearingroom;
    private String hearinghandicapetc;
    private String blindhandicapetc;
    private String handicapetc;
    private String publictransport;

    private String ticketoffice;
    private String guidehuman;

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

        private String a11yParking;
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
        private String hearingroom;
        private String hearinghandicapetc;
        private String blindhandicapetc;
        private String handicapetc;
        private String publictransport;
        private String ticketoffice;
        private String guidehuman;
    }

    public static AttractionDetailResponse of(
            AttractionResponse base,
            AttractionDetailResponse.IntroNormalized intro,
            AttractionDetailResponse.DetailCommon common,
            AttractionIntro a11y
    ) {
        String image = firstNonBlank(base.getFirstImage(), base.getFirstImage2());

        // ✅ tel: common.infocenter → intro.infocenter → base.tel 순으로 안전한 폴백
        String tel = firstNonBlank(
                common != null ? common.getInfocenter() : null,
                intro != null ? intro.getInfocenter() : null,
                base.getTel()
        );

        String addr = firstNonBlank(
                joinNonBlank(" ", base.getAddr1(), base.getAddr2())
        );

        return AttractionDetailResponse.builder()
                .contentId(base.getContentId())
                .title(base.getTitle())
                .image(image)
                .tel(tel)
                .addr(addr)
                .useTime(firstNonBlank(intro != null ? intro.getUsetime() : null))
                .restDate(firstNonBlank(intro != null ? intro.getRestdate() : null))
                .parking(firstNonBlank(intro != null ? intro.getParking() : null))
                .a11yParking(a11y != null ? a11y.getA11yParking() : null)
                .age(firstNonBlank(intro != null ? intro.getAge() : null))

                .overview(firstNonBlank(
                        common != null ? common.getOverview() : null,
                        a11y  != null ? a11y.getOverview() : null
                ))

                .attractionId(base.getAttractionId())

                .wheelchair(a11y != null ? a11y.getWheelchair() : null)
                .elevator(a11y != null ? a11y.getElevator() : null)
                .braileblock(a11y != null ? a11y.getBraileblock() : null)
                .exit(a11y != null ? a11y.getExit() : null)
                .guidesystem(a11y != null ? a11y.getGuidesystem() : null)
                .signguide(a11y != null ? a11y.getSignguide() : null)
                .videoguide(a11y != null ? a11y.getVideoguide() : null)
                .audioguide(a11y != null ? a11y.getAudioguide() : null)
                .bigprint(a11y != null ? a11y.getBigprint() : null)
                .brailepromotion(a11y != null ? a11y.getBrailepromotion() : null)
                .helpdog(a11y != null ? a11y.getHelpdog() : null)
                .hearingroom(a11y != null ? a11y.getHearingroom() : null)
                .hearinghandicapetc(a11y != null ? a11y.getHearinghandicapetc() : null)
                .blindhandicapetc(a11y != null ? a11y.getBlindhandicapetc() : null)
                .handicapetc(a11y != null ? a11y.getHandicapetc() : null)
                .publictransport(a11y != null ? a11y.getPublictransport() : null)

                // 새로 추가된 필드
                .ticketoffice(a11y != null ? a11y.getTicketoffice() : null)
                .guidehuman(a11y != null ? a11y.getGuidehuman() : null)

                .lat(base.getMapY())
                .lon(base.getMapX())
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
                .reduce((a, b) -> a + sep + b)
                .orElse(null);
    }
}