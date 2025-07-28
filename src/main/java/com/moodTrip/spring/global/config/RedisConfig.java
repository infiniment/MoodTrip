package com.moodTrip.spring.global.config;

import com.moodTrip.spring.domain.chat.service.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    private final RedisSubscriber redisSubscriber; // Redis 메시지 수신 후 WebSocket으로 전달하는 역할

    /*
    *  MessageListenerAdapter Bean
    * - Redis에서 수신한 메시지를 처리할 메서드 연결
    * - "sendMessage"는 RedisSubscriber의 메서드 이름과 동일해야 함
    * */
    @Bean
    public MessageListenerAdapter listenerAdapter() {
        return new MessageListenerAdapter(redisSubscriber, "sendMessage");
    }

    /*
    *  PatternTopic Bean
    * - chatroom:* 형식의 모든 채팅방 메시지를 구독 ex) chatroom: 1, chatroom: 2 등
    * */
    @Bean
    public PatternTopic patternTopic() {
        return new PatternTopic("chatroom:*");
    }

    /*
    * RedisMessageListenerContainer Bean
    * - Redis Pub/Sub 메시지를 실시간으로 수신
    * - listenerAdapter와 patternTopic을 주입받아 사용
    * */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter,
            PatternTopic patternTopic)
    {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, patternTopic);
        return container;
    }

    /*
     *  RedisTemplate Bean
     * - Redis에 JSON 데이터를 저장하고 불러오기 위한 직렬화 방식 지정
     * - 채팅 메시지를 JSON으로 주고받을 때 사용
     * */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Key는 String, Value는 JSON으로 직렬화
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

}
