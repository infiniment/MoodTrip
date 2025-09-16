package com.moodTrip.spring.domain.enteringRoom.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveRequest {
    // 승인 요청 dto
    private Long requestId;
    private String approvalMessage; // 선택적 승인 메시지
}