package com.moodTrip.spring.domain.chat.dto.response;

import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private String sender; // 보낸 사람 닉네임
    private String message; // 메시지 내용z
    private LocalDateTime sendTime; // 보낸 시간

    /*
    * ChatMessageRequest → ChatMessageResponse 변환 메서드
    * - RedisSubscriber에서 사용
    * */
    public static ChatMessageResponse fromRequest(String sender, String message) {
        return ChatMessageResponse.builder()
                .sender(sender)
                .message(message)
                .sendTime(LocalDateTime.now())  // 서버 기준 시간
                .build();
    }
}
