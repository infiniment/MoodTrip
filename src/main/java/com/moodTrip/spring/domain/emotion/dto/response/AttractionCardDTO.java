package com.moodTrip.spring.domain.emotion.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter // ✅ final 제거, Setter 추가
@NoArgsConstructor // ✅ Builder와 함께 사용하기 위해 추가
public class AttractionCardDTO {

    // ✅ 필드 이름을 attractionId로 통일
    private Long attractionId;
    private String title;
    private String addr1;
    private String firstImage;
    private String description;

    // ✅ '좋아요' 상태를 담을 필드 추가
    private boolean isLikedByUser = false;

    @Builder
    public AttractionCardDTO(Long attractionId, String title, String addr1, String firstImage, String description) {
        this.attractionId = attractionId;
        this.title = title;
        this.addr1 = addr1;
        this.firstImage = firstImage;
        this.description = description;
    }
}
