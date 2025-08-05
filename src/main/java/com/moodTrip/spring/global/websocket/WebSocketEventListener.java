package com.moodTrip.spring.global.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final OnlineUserTracker onlineUserTracker;
    private final SimpMessagingTemplate messagingTemplate;

    // 웹소켓 연결 시
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        // 여기서는 별도 처리 안함 (입장 메시지에서 처리됨)
        log.info("WebSocket 연결됨");
    }

    // 웹소켓 연결 해제 시
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes == null) return;

        Object roomIdObj = sessionAttributes.get("roomId");
        Object nicknameObj = sessionAttributes.get("nickname");

        if (roomIdObj instanceof Long roomId && nicknameObj instanceof String nickname) {
            onlineUserTracker.removeUser(roomId, nickname);

            messagingTemplate.convertAndSend("/sub/chatroom/" + roomId + "/users",
                    onlineUserTracker.getOnlineUsers(roomId));
        }
    }
}
