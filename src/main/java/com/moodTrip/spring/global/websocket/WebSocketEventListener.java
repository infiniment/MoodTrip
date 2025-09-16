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

        // ChatService에서 put한 키 이름과 일치 시키기: "chattingRoomId", "nickname"
        Object roomIdObj = sessionAttributes.get("chattingRoomId");
        Object nicknameObj = sessionAttributes.get("nickname");

        Long chattingRoomId = toLong(roomIdObj);
        String nickname = String.valueOf(nicknameObj);
        if (chattingRoomId == null || nickname == null || nickname.isBlank()) return;

        // 1) 온라인 목록에서 제거 + 목록 브로드캐스트
        onlineUserTracker.removeUser(chattingRoomId, nickname);
        messagingTemplate.convertAndSend(
                "/sub/chatroom/" + chattingRoomId + "/users",
                onlineUserTracker.getOnlineUsers(chattingRoomId)
        );

        // 2) 퇴장 시스템 메시지 브로드캐스트
        var payload = Map.of(
                "chattingRoomId", chattingRoomId,
                "sender", nickname,
                "message", nickname + "님이 퇴장했습니다.",
                "type", "LEAVE",
                "sendTime", java.time.LocalDateTime.now().toString()
        );

        messagingTemplate.convertAndSend("/sub/chatroom/" + chattingRoomId, payload);

        log.info("WebSocket 해제 처리: roomId={}, nickname={}", chattingRoomId, nickname);
    }

    private Long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); }
        catch (Exception e) { return null; }
    }
}
