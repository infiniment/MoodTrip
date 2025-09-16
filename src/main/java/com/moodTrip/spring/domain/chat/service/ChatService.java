package com.moodTrip.spring.domain.chat.service;

import com.moodTrip.spring.domain.chat.dto.request.ChatMessageRequest;
import com.moodTrip.spring.global.websocket.OnlineUserTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final RedisPublisher redisPublisher;
    private final OnlineUserTracker onlineUserTracker;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatHistoryCacheService chatHistoryCacheService;

    private static final int KEEP_LAST_N = 200;     // 캐시에 유지할 총 개수
    private static final int SEND_ON_ENTER_N = 50;  // 입장 시 한번에 줄 개수

    public void sendMessage(ChatMessageRequest message, SimpMessageHeaderAccessor accessor) {
        Long chattingRoomId = message.getChattingRoomId();
        String sender = message.getSender();

        // 기본 방어
        if (chattingRoomId == null) {
            log.warn("chattingRoomId is null. message: {}", message);
            return;
        }

        // 1) 타입별 부가 로직
        switch (String.valueOf(message.getType())) {
            case "ENTER" -> {
                accessor.getSessionAttributes().put("chattingRoomId", chattingRoomId);
                accessor.getSessionAttributes().put("nickname", sender);

                onlineUserTracker.addUser(chattingRoomId, sender);

                // 접속자 목록 브로드캐스트
                messagingTemplate.convertAndSend(
                        "/sub/chatroom/" + chattingRoomId + "/users",
                        onlineUserTracker.getOnlineUsers(chattingRoomId)
                );

                // 입장한 "그 세션"에게만 최근 히스토리 1회 전송
                String sessionId = accessor.getSessionId();
                var recent = chatHistoryCacheService.recentByChatId(chattingRoomId, SEND_ON_ENTER_N);
                messagingTemplate.convertAndSendToUser(
                        sessionId,
                        "/queue/chat-history",
                        recent,
                        headersFor(sessionId)
                );
            }
            case "LEAVE" -> {
                // 선택: 나가기 메시지일 경우 온라인 사용자 목록에서 제거
                onlineUserTracker.removeUser(chattingRoomId, sender);
                messagingTemplate.convertAndSend(
                        "/sub/chatroom/" + chattingRoomId + "/users",
                        onlineUserTracker.getOnlineUsers(chattingRoomId)
                );
            }
            default -> {
                // TALK 등: 별도 처리 없음
            }
        }

        // 2) 모든 메시지를 캐시에 적재 (최근 N개 유지)
        chatHistoryCacheService.appendByChatId(chattingRoomId, toCacheObject(message), KEEP_LAST_N);

        // 3) Redis Pub/Sub 발행(기존 흐름 유지)
        ChannelTopic topic = new ChannelTopic("chatroom:" + chattingRoomId);
        redisPublisher.publish(topic, message);
    }

    private Map<String, Object> toCacheObject(ChatMessageRequest m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("chattingRoomId", m.getChattingRoomId());
        map.put("sender", m.getSender());
        map.put("message", m.getMessage());
        map.put("type", m.getType());
        map.put("sentAt", System.currentTimeMillis());
        return map;
    }

    private MessageHeaders headersFor(String sessionId) {
        SimpMessageHeaderAccessor ha = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        ha.setSessionId(sessionId);
        ha.setLeaveMutable(true);
        return ha.getMessageHeaders();
    }
}