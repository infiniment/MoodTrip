package com.moodTrip.spring.domain.chat.service;

import com.moodTrip.spring.domain.chat.dto.request.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RedisPublisher redisPublisher;

    /*
    * 채팅 메시지를 Redius Pub/Sub으로 발행
    * @Param message : 클라이언트에서 전달한 메시지
    * */
    public void sendMessage(ChatMessageRequest message) {
        // 채널 이름 : chatroom:{roomId}
        ChannelTopic topic = new ChannelTopic("chatroom:" + message.getChattingRoomId());
        redisPublisher.publish(topic, message);
    }
}
