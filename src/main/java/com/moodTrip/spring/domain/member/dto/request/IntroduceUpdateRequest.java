package com.moodTrip.spring.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "자기소개 수정 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntroduceUpdateRequest {

    @Schema(
            description = "수정할 자기소개 내용",
            example = "안녕하세요! 여행을 사랑하는 김상우입니다. 함께 즐거운 추억을 만들어요! 🌍✈️"
    )
    private String profileBio;
}