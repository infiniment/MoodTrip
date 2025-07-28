package com.moodTrip.spring.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    /*
    * Redis에 메시지 발행(Publish)
    * @Param topic : 발행할 채널 (ex : chatroom:1)
    * @Param message : 발행할 메시지 (JSON 형태)
    * */
    public void publish(ChannelTopic topic, Object message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
