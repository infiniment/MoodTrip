package com.moodTrip.spring.domain.enteringRoom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionResponse {

    // 승인/거절 처리 결과 dto
    private boolean success;
    private String message;
    private LocalDateTime processedAt;
    private List<String> processedNames;    // 처리된 신청자 이름들
    private Integer totalProcessed;         // 처리된 건수

    // 성공 시 응답
    public static ActionResponse success(String message, List<String> names) {
        return ActionResponse.builder()
                .success(true)
                .message(message)
                .processedAt(LocalDateTime.now())
                .processedNames(names)
                .totalProcessed(names.size())
                .build();
    }

    // 실패 시 응답
    public static ActionResponse failure(String message) {
        return ActionResponse.builder()
                .success(false)
                .message(message)
                .processedAt(LocalDateTime.now())
                .processedNames(List.of())
                .totalProcessed(0)
                .build();
    }
}