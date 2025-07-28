//package com.moodTrip.spring.global.websocket;
//
//import com.moodTrip.spring.global.security.jwt.JwtTokenProvider;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
//
//import java.util.Map;
//
//@Slf4j
//@RequiredArgsConstructor
//public class CustomHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
//    private final JwtTokenProvider jwtTokenProvider;
//
//    @Override
//    public boolean beforeHandshake(ServerHttpRequest request,
//                                   ServerHttpResponse response,
//                                   WebSocketHandler wsHandler,
//                                   Map<String, Object> attributes) throws Exception {
//        // 토큰 추출 ex) ws://localhost:8080/ws/chat?token=JWT_TOKEN
//        String token = getTokenFromQuery(request);
//
//        if(token != null && jwtTokenProvider.validateToken(token)) {
//            String userId = jwtTokenProvider.getUserId(token);
//            attributes.put("userId", userId); // 세션에 저장
//            log.info("Hanshake 성공 : userId = {}", userId);
//            return super.beforeHandshake(request, response, wsHandler, attributes);
//        }else {
//            log.warn("Handshake 실패 : 유효하지 않은 토큰");
//            return false; // 연결 거부
//        }
//
//    }
//
//    private String getTokenFromQuery(ServerHttpRequest request) {
//        String query = request.getURI().getQuery();
//        if(query != null && query.startsWith("token=")) {
//            return query.substring("token=".length());
//        }
//        return null;
//    }
//}
