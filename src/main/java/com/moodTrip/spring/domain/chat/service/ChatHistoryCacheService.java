package com.moodTrip.spring.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatHistoryCacheService {

    private final RedisTemplate<String, Object> redis;

    private String key(Long chattingRoomId) {
        return "chat:room:" + chattingRoomId + ":messages";
    }

    /** 최근 N개만 보존 */
    public void appendByChatId(Long chattingRoomId, Object msg, int keepLastN) {
        String k = key(chattingRoomId);
        redis.opsForList().rightPush(k, msg);
        redis.opsForList().trim(k, -keepLastN, -1);
    }

    /** 최근 n개 조회 (오래된 것 → 최신 순서) */
    public List<Object> recentByChatId(Long chattingRoomId, int n) {
        String k = key(chattingRoomId);
        Long size = redis.opsForList().size(k);
        if (size == null || size == 0) return List.of();
        long start = Math.max(0, size - n);
        return redis.opsForList().range(k, start, size - 1);
    }
}