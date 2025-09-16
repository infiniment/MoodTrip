package com.moodTrip.spring.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocket 메시지 브로커 구성
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/sub", "/queue");        // 구독 경로 (메시지 받는 경로)
        config.setApplicationDestinationPrefixes("/pub"); // 발행 경로 (메시지 보내는 경로)
        // ✅ user destination prefix (convertAndSendToUser(...) 에 필요)
        config.setUserDestinationPrefix("/user");
    }

    //클라이언트의 엔드포인트를 등록하고 SockJS를 사용하도록 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")          // 클라이언트 접속 URL // ex ) ws://localhost:8080/ws/chat
                .setAllowedOriginPatterns("*")    // 개발 중 모든 CORS 허용
                .withSockJS();                    // SockJS 지원
    }
}
