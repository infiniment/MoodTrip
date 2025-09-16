package com.moodTrip.spring.domain.chat.controller;


import com.moodTrip.spring.domain.chat.dto.request.ChatMessageRequest;
import com.moodTrip.spring.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    /*
    * 클라이언트가 /pub/chat/message 경로로 메시지를 발행하면 실행
    * @Param message : 클라이언트가 보낸 채팅 메시지
    * */
    @MessageMapping("/chat/message")
    public void message(ChatMessageRequest message, SimpMessageHeaderAccessor accessor) {
        chatService.sendMessage(message, accessor);
    }
}
