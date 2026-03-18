package com.moodTrip.spring.domain.chat.dto.request;

import com.moodTrip.spring.domain.chat.enums.MessageType;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ChatMessageRequest {
    private MessageType type; // 메시지 타입
    private Long chattingRoomId; // 채팅방 ID
    private String sender; // 보낸 사람 닉네임
    private String message; // 메시지 내용
}
