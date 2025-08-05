package com.moodTrip.spring.domain.schedule.controller;

import com.moodTrip.spring.global.websocket.ChatMessage;
import com.moodTrip.spring.global.websocket.OnlineUserTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SchedulingWebSocketController {
    private final OnlineUserTracker userTracker;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/scheduling/enter")
    public void enter(ChatMessage message, SimpMessageHeaderAccessor accessor) {
        Long roomId = message.getRoomId();
        String nickname = message.getSender();

        log.info("✅ [ENTER] {} 님이 방 {} 에 입장", nickname, roomId); // 추가
        accessor.getSessionAttributes().put("roomId", roomId);
        accessor.getSessionAttributes().put("nickname", nickname);

        userTracker.addUser(roomId, nickname);

        messagingTemplate.convertAndSend("/sub/scheduling/" + roomId, userTracker.getOnlineUsers(roomId));
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Long roomId = (Long) accessor.getSessionAttributes().get("roomId");
        String nickname = (String) accessor.getSessionAttributes().get("nickname");

        if (roomId != null && nickname != null) {
            userTracker.removeUser(roomId, nickname);

            messagingTemplate.convertAndSend(
                    "/sub/scheduling/" + roomId,
                    userTracker.getOnlineUsers(roomId)
            );
        }
    }
}
