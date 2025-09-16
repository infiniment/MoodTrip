package com.moodTrip.spring.domain.schedule.controller;

import com.moodTrip.spring.global.websocket.ChatMessage;
import com.moodTrip.spring.global.websocket.OnlineUserTracker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingWebSocketControllerTest {

    @Mock OnlineUserTracker userTracker;
    @Mock SimpMessagingTemplate messagingTemplate;

    @InjectMocks SchedulingWebSocketController controller;

    private SimpMessageHeaderAccessor newAccessorWithSession(Map<String, Object> sessionAttrs) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionId("sess-1");
        accessor.setLeaveMutable(true); // headers가 message로 복사된 후에도 읽기 가능
        accessor.setSessionAttributes(sessionAttrs);
        return accessor;
    }

    @Test
    @DisplayName("enter: 세션에 roomId/nickname 저장하고 온라인 목록을 브로드캐스트")
    void enter_ok() {
        // given
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(10L);
        msg.setSender("Alice");

        Map<String, Object> session = new HashMap<>();
        SimpMessageHeaderAccessor accessor = newAccessorWithSession(session);

        when(userTracker.getOnlineUsers(10L)).thenReturn(List.of("Alice", "Bob"));

        // when
        controller.enter(msg, accessor);

        // then
        // 세션 저장 확인
        assertThat(accessor.getSessionAttributes().get("roomId")).isEqualTo(10L);
        assertThat(accessor.getSessionAttributes().get("nickname")).isEqualTo("Alice");

        // 트래커/브로드캐스트 호출 검증
        verify(userTracker).addUser(10L, "Alice");
        verify(userTracker).getOnlineUsers(10L);

        ArgumentCaptor<String> destCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCap = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(destCap.capture(), payloadCap.capture());

        assertThat(destCap.getValue()).isEqualTo("/sub/schedule/10");
        assertThat(payloadCap.getValue()).isEqualTo(List.of("Alice", "Bob"));
    }

    @Test
    @DisplayName("disconnect: 세션 정보로 removeUser 후 온라인 목록 브로드캐스트")
    void disconnect_ok() {
        // given: 세션에 roomId/nickname이 저장되어 있다고 가정
        Map<String, Object> session = new HashMap<>();
        session.put("roomId", 10L);
        session.put("nickname", "Alice");
        SimpMessageHeaderAccessor accessor = newAccessorWithSession(session);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, "sess-1", CloseStatus.NORMAL);

        when(userTracker.getOnlineUsers(10L)).thenReturn(List.of("Bob"));

        // when
        controller.handleDisconnect(event);

        // then
        verify(userTracker).removeUser(10L, "Alice");
        verify(userTracker).getOnlineUsers(10L);
        verify(messagingTemplate).convertAndSend("/sub/schedule/10", List.of("Bob"));
    }

    @Test
    @DisplayName("disconnect: 세션 정보가 없으면 아무것도 하지 않음")
    void disconnect_noSessionAttrs_noop() {
        // given: roomId, nickname 없음
        Map<String, Object> session = new HashMap<>();
        SimpMessageHeaderAccessor accessor = newAccessorWithSession(session);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, message, "sess-1", CloseStatus.NORMAL);

        // when
        controller.handleDisconnect(event);

        // then
        verifyNoInteractions(userTracker, messagingTemplate);
    }
}
