package com.moodTrip.spring.domain.chat.service;

import com.moodTrip.spring.domain.chat.dto.request.ChatMessageRequest;
import com.moodTrip.spring.global.websocket.OnlineUserTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final RedisPublisher redisPublisher;
    private final OnlineUserTracker onlineUserTracker;
    private final SimpMessagingTemplate messagingTemplate;

    /*
     * 채팅 메시지를 Redis Pub/Sub으로 발행
     */
    public void sendMessage(ChatMessageRequest message, SimpMessageHeaderAccessor accessor) {
        Long chattingRoomId = message.getChattingRoomId();
        String sender = message.getSender();

        // 입장 메시지일 경우 세션 저장 + 접속자 목록 업데이트
        if ("ENTER".equals(message.getType())) {
            accessor.getSessionAttributes().put("chattingRoomId", chattingRoomId);
            accessor.getSessionAttributes().put("nickname", sender);

            onlineUserTracker.addUser(chattingRoomId, sender);

            // 접속자 목록 브로드캐스트
            messagingTemplate.convertAndSend("/sub/chatroom/" + chattingRoomId + "/users",
                    onlineUserTracker.getOnlineUsers(chattingRoomId));
        }

        // 채팅 메시지를 Redis로 발행
        ChannelTopic topic = new ChannelTopic("chatroom:" + chattingRoomId);
        redisPublisher.publish(topic, message);
    }
}