package com.moodTrip.spring.global.websocket;

import com.moodTrip.spring.domain.rooms.service.RoomAuthService;
import com.moodTrip.spring.global.common.code.status.ErrorStatus;
import com.moodTrip.spring.global.common.exception.CustomException;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

// 실제 검사 로직
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private final RoomAuthService roomAuthService; // 내부적으로 RoomMemberRepository 사용
    private static final String SUB_PREFIX = "/sub/schedule/room/";
    private static final String PUB_PREFIX = "/pub/schedule/room/";

    private Long extractRoomId(String destination, String prefix) {
        String tail = destination.substring(prefix.length());
        String firstSeg = tail.split("/")[0]; // 뒤에 뭐가 와도 첫 세그먼트만
        return Long.valueOf(firstSeg);
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;



        var auth = (Authentication) accessor.getUser();
        if (auth == null || !(auth.getPrincipal() instanceof MyUserDetails user)) {
            throw new CustomException(ErrorStatus.WEBSOCKET_FORBIDDEN);
        }
        Long memberPk = user.getMember().getMemberPk();

        String destination = accessor.getDestination();
        if (destination == null) return message;

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) && destination.startsWith(SUB_PREFIX)) {
            Long roomId = extractRoomId(destination, SUB_PREFIX);
            roomAuthService.assertActiveMember(roomId, memberPk);
        }
        if (StompCommand.SEND.equals(accessor.getCommand()) && destination.startsWith(PUB_PREFIX)) {
            Long roomId = extractRoomId(destination, PUB_PREFIX);
            roomAuthService.assertActiveMember(roomId, memberPk);
        }

        return message;
    }

}
