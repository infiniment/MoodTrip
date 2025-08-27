package com.moodTrip.spring.domain.attraction.dto.response;


import com.moodTrip.spring.domain.attraction.entity.Attraction; // Attraction 엔티티 경로
import lombok.Getter;

@Getter
public class AttractionSearchResponseDto {

    // JavaScript의 renderResults 함수에서 사용하는 필드들
    private Long attractionId;
    private String title;
    private String firstImage;
    private String addr1;
    private String description; // JS에서는 description으로 사용, Attraction 엔티티의 필드명에 맞게 수정 필요

    // 찜 기능을 위해 추가된 가장 중요한 필드
    private boolean isLikedByCurrentUser;

    // 생성자: Attraction 엔티티와 찜 여부(boolean)를 받아 DTO를 생성
    public AttractionSearchResponseDto(Attraction attraction, boolean isLiked) {
        this.attractionId = attraction.getAttractionId(); // Attraction 엔티티의 ID getter
        this.title = attraction.getTitle(); // ERD 기반 'attractionname'
        this.firstImage = attraction.getFirstImage(); // Attraction 엔티티의 이미지 getter (필요시 추가)
        this.addr1 = attraction.getAddr1(); // ERD 기반 'attractionaddress'
        this.isLikedByCurrentUser = isLiked;
    }
}