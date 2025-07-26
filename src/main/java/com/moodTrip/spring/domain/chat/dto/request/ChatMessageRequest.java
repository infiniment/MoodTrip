package com.moodTrip.spring.domain.chat.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    // 메시지 타입(입장, 일반 대화, 퇴장)
    public enum MessageType {
        ENTER, TALK, LEAVE
    }

    private MessageType type; // 메시지 타입
    private Long chattingRoomId; // 채팅방 ID
    private String sender; // 보낸 사람 닉네임
    private String message; // 메시지 내용
}
