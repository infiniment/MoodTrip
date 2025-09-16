package com.moodTrip.spring.domain.enteringRoom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 방 입장 신청 dto
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinResponse {
    private Long joinRequestId;
    private Long roomId;
    private String applicantNickname;
    private String message;
    private LocalDateTime appliedAt;
    private String status;
    private String resultMessage;
    private boolean success;
}