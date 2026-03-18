package com.moodTrip.spring.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.chat.event.ChatEvent;
import com.moodTrip.spring.domain.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    // WebSocket 구독자에게 메시지
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Redis에서 발행된 메시지 역직렬화(json)
            String publishMessage = new String(message.getBody());

            // JSON -> ChatMessageRequest 변환
            ChatEvent event = objectMapper.readValue(publishMessage, ChatEvent.class);

            // 응답 DTO로 변환
            ChatMessageResponse response = ChatMessageResponse.fromRequest(event.sender(), event.message());

            // WebSocket 구독자에게 메시지 전송
            messagingTemplate.convertAndSend("/sub/chatroom/" + event.chattingRoomId(), response);

            log.info("Redis → WebSocket 전송 완료 : chattingRoomId={}, sender={}, message={}",
                    event.chattingRoomId(), event.sender(), event.message());
        }catch (Exception e) {
            log.error("RedisSubscriber 오류 : {}", e.getMessage());
        }
    }
}
