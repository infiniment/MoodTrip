//package com.moodTrip.spring.global.websocket;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class CustomChannelInterceptor implements ChannelInterceptor {
//    private final RoomAuthService roomAuthService; // 방 참여 여부 검증 서비스
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//
//        if(accessor.getCommand() != null) {
//            String userId = (String) accessor.getSessionAttributes().get("userId");
//            Long roomId = getRoomIdFromDestination(accessor.getDestination());
//
//            if(!roomAuthService.isUserInRoom(userId, roomId)) {
//                log.warn("방 참여자 아님 : userId={}, roomId={}", userId, roomId);
//                throw new IllegalArgumentException("방 참여자가 아닙니다.");
//            }
//
//            log.info("메시지 허용 : userId={}, roomId={}", userId, roomId);
//        }
//        return message;
//    }
//
//    private Long getRoomIdFromDestination(String destination) {
//        if (destination != null && destination.startsWith("/sub/chatroom/")) {
//            return Long.valueOf(destination.replace("/sub/chatroom/", ""));
//        }
//        return null;
//    }
//}
