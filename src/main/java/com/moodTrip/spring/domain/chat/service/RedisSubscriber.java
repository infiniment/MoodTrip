package com.moodTrip.spring.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.chat.dto.request.ChatMessageRequest;
import com.moodTrip.spring.domain.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
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
            // Redis에서 발행된 메시지 역직렬화
            String publishMessage = new String(message.getBody());

            // JSON -> ChatMessageRequest 변환
            ChatMessageRequest request = objectMapper.readValue(publishMessage, ChatMessageRequest.class);

            // 응답 DTO로 변환
            ChatMessageResponse response = ChatMessageResponse.fromRequest(request.getSender(), request.getMessage());

            // WebSocket 구독자에게 메시지 전송
            messagingTemplate.convertAndSend("/sub/chatroom/" + request.getChattingRoomId(), response);

            log.info("Redis → WebSocket 전송 완료 : chattingRoomId={}, sender={}, message={}",
                    request.getChattingRoomId(), request.getSender(), request.getMessage());
        }catch (Exception e) {
            log.error("RedisSubscriber 오류 : {}", e.getMessage());
        }
    }
}
