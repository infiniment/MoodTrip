package com.moodTrip.spring.domain.fire.dto.response;

import com.moodTrip.spring.domain.fire.entity.MemberFire;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberFireResponse {

    private boolean success;
    private String message;
    private Long fireId;
    private Long roomId;
    private String reportedNickname;
    private LocalDateTime firedAt;
    private String fireReason;

    public static MemberFireResponse success(MemberFire fire) {
        return MemberFireResponse.builder()
                .success(true)
                .message("신고가 접수되었습니다. 운영진이 확인 후 조치합니다.")
                .fireId(fire.getFireId())
                .roomId(fire.getTargetRoom().getRoomId())
                .reportedNickname(fire.getReportedMember().getNickname())
                .firedAt(fire.getCreatedAt())
                .fireReason(fire.getFireReason().getDescription())
                .build();
    }

    public static MemberFireResponse failure(String message) {
        return MemberFireResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
