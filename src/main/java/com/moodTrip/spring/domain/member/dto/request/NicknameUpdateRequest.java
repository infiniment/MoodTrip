package com.moodTrip.spring.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "닉네임 수정 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NicknameUpdateRequest {

    @Schema(
            description = "닉네임 수정",
            example = "새로운닉네임123"
    )
    private String nickname;

}