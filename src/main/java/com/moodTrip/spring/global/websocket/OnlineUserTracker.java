package com.moodTrip.spring.global.websocket;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Slf4j
@Component
public class OnlineUserTracker { // 접속자 관리 컴포넌트
    // roomId -> 접속자 nickname 리스트
    private final Map<Long, Set<String>> schedulingRoomUsers = new ConcurrentHashMap<>();

    public void addUser(Long roomId, String nickname) {
        schedulingRoomUsers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet()).add(nickname);
        log.info("{}님이 방 {}에 접속. 현재 접속자 : {}", nickname, roomId, schedulingRoomUsers.get(roomId));
    }

    public void removeUser(Long roomId, String nickname) {
        Set<String> users = schedulingRoomUsers.get(roomId);
        if(users != null) {
            users.remove(nickname);
            log.info("{}님이 방 {}에서 퇴장. 남은 접속자 : {}", nickname, roomId, users);
            if(users.isEmpty()) {
                schedulingRoomUsers.remove(roomId);
            }
        }
    }

    public List<String> getOnlineUsers(Long roomId) {
        return new ArrayList<>(schedulingRoomUsers.getOrDefault(roomId, Collections.emptySet()));
    }

    public Map<Long, Set<String>> getAllRooms() {
        return schedulingRoomUsers;
    }

}
