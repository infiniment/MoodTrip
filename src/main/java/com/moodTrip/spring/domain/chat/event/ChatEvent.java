package com.moodTrip.spring.domain.chat.event;

import com.moodTrip.spring.domain.chat.enums.MessageType;
import com.moodTrip.spring.domain.chat.exception.ChatException;
import lombok.Builder;

@Builder
public record ChatEvent(
        String eventId,
        Long chattingRoomId,
        MessageType type,
        String sender,
        String message,
        long sentAt
) {
    public static ChatEvent of(Long roomId, MessageType type, String sender, String message) {
        return new ChatEvent(
                java.util.UUID.randomUUID().toString(),
                roomId,
                type,
                sender,
                message,
                System.currentTimeMillis()
        );
    }

    public void validate() {
        if (chattingRoomId == null) throw ChatException.invalidChatRoomId();
        if (type == null) throw ChatException.invalidMessageType();
        if (sender == null || sender.isBlank()) throw ChatException.invalidSender();
        if (type == MessageType.TALK && (message == null || message.isBlank())) {
            throw ChatException.emptyMessage();
        }
    }
}
