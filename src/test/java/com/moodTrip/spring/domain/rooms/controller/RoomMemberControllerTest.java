package com.moodTrip.spring.domain.rooms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.dto.response.RoomMemberResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.global.common.code.status.ErrorStatus;
import com.moodTrip.spring.global.common.exception.CustomException;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import com.moodTrip.spring.global.web.GlobalControllerAdvice; // ⬅️ 실제 전역 핸들러로 교체하세요(프로젝트 클래스명)
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import com.moodTrip.spring.global.websocket.OnlineUserTracker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = RoomMemberController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.moodTrip.spring.global.web.GlobalControllerAdvice.class // 뷰(Thymeleaf) 에러 페이지 렌더링 어드바이스
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.moodTrip.spring.global.common.exception.GlobalExceptionHandler.class)  // JSON 에러 어드바이스 주입
class RoomMemberControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    @MockitoBean
    RoomService roomService;
    @MockitoBean RoomRepository roomRepository;
    @MockitoBean OnlineUserTracker onlineUserTracker;

    // 기존 다른 Advice 들이 의존하는 유틸/서비스 목 처리
    @MockitoBean SecurityUtil securityUtil;
    @MockitoBean com.moodTrip.spring.domain.emotion.service.EmotionService emotionService;


    // ====== 헬퍼: 인증 주입 ======
    private void setAuth(MyUserDetails principal) {
        var auth = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        var ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
    }

    private MyUserDetails stubUserDetails(Long memberPk) {
        Member member = Mockito.mock(Member.class);
        when(member.getMemberPk()).thenReturn(memberPk);
        MyUserDetails mud = Mockito.mock(MyUserDetails.class);
        when(mud.getMember()).thenReturn(member);
        when(mud.getUsername()).thenReturn("tester");
        return mud;
    }

    // ====== join ======
    @Test
    @DisplayName("POST /api/v1/room-members/{id}/join - 참여 성공 200")
    void joinRoom_200() throws Exception {
        Long roomId = 1L;
        Room room = Mockito.mock(Room.class);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomService.isMemberInRoom(any(Member.class), eq(room))).thenReturn(false);
        doNothing().when(roomService).joinRoom(any(Member.class), eq(room), eq("MEMBER"));

        setAuth(stubUserDetails(100L));

        mockMvc.perform(
                post("/api/v1/room-members/{roomId}/join", roomId)
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/room-members/{id}/join - 이미 참여한 유저 409")
    void joinRoom_409() throws Exception {
        Long roomId = 1L;
        Room room = Mockito.mock(Room.class);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomService.isMemberInRoom(any(Member.class), eq(room))).thenReturn(true);

        setAuth(stubUserDetails(100L)); // 인증 주입 (이미 있으시면 유지)

        mockMvc.perform(post("/api/v1/room-members/{roomId}/join", roomId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ROOM_MEMBER_001"))
                .andExpect(jsonPath("$.message").value("이미 해당 방에 참여한 회원입니다."))
                .andExpect(jsonPath("$.data").doesNotExist()); // 혹은 isEmpty()
    }

    @Test
    @DisplayName("POST /api/v1/room-members/{id}/join - 방 없음 404")
    void joinRoom_404() throws Exception {
        Long roomId = 999L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        setAuth(stubUserDetails(100L));

        mockMvc.perform(
                        post("/api/v1/room-members/{roomId}/join", roomId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ROOM_001"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 방입니다."))
                .andExpect(jsonPath("$.data").doesNotExist()); // 또는 .value(Matchers.nullValue())
    }

    // ====== leave ======
    @Test
    @DisplayName("DELETE /api/v1/room-members/{id}/leave - 나가기 성공 204")
    void leaveRoom_204() throws Exception {
        Long roomId = 1L;
        Room room = Mockito.mock(Room.class);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        doNothing().when(roomService).leaveRoom(any(Member.class), eq(room));

        setAuth(stubUserDetails(100L));

        mockMvc.perform(
                delete("/api/v1/room-members/{roomId}/leave", roomId)
        ).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/room-members/{id}/leave - 방 없음 404")
    void leaveRoom_404() throws Exception {
        Long roomId = 999L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        setAuth(stubUserDetails(100L));

        mockMvc.perform(
                        delete("/api/v1/room-members/{roomId}/leave", roomId)
                                .accept(MediaType.APPLICATION_JSON) // ← 핵심: JSON으로 협상
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ROOM_001"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 방입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // ====== get members ======
    @Test
    @DisplayName("GET /api/v1/room-members/{id}/members - 참여자 목록 200")
    void getActiveMembers_200() throws Exception {
        Long roomId = 1L;
        Room room = Mockito.mock(Room.class);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        var m1 = RoomMemberResponse.builder().nickname("alice").build();
        var m2 = RoomMemberResponse.builder().nickname("bob").build();
        when(roomService.getActiveMembers(room)).thenReturn(List.of(m1, m2));

        mockMvc.perform(
                        get("/api/v1/room-members/{roomId}/members", roomId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/room-members/{id}/members - 방 없음 404")
    void getActiveMembers_404() throws Exception {
        Long roomId = 999L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/v1/room-members/{roomId}/members", roomId)
                                .accept(MediaType.APPLICATION_JSON) // ← 핵심
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ROOM_001")) // 프로젝트 ErrorStatus에 맞게
                .andExpect(jsonPath("$.message").value("존재하지 않는 방입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
    // ====== get online members ======
    @Test
    @DisplayName("GET /api/v1/room-members/{id}/online-members - 온라인 참여자만 필터링 200")
    void getOnlineMembers_200() throws Exception {
        Long roomId = 1L;
        Room room = Mockito.mock(Room.class);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        var all = List.of(
                RoomMemberResponse.builder().nickname("alice").build(),
                RoomMemberResponse.builder().nickname("bob").build()
        );
        when(roomService.getActiveMembers(room)).thenReturn(all);
        when(onlineUserTracker.getOnlineUsers(roomId)).thenReturn(List.of("bob")); // bob만 온라인

        mockMvc.perform(
                        get("/api/v1/room-members/{roomId}/online-members", roomId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nickname").value("bob"));
    }

    @Test
    @DisplayName("GET /api/v1/room-members/{id}/online-members - 방 없음 404")
    void getOnlineMembers_404() throws Exception {
        Long roomId = 999L;
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/v1/room-members/{roomId}/online-members", roomId)
                                .accept(MediaType.APPLICATION_JSON) // ← 핵심
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                // 프로젝트의 코드/메시지에 맞춰 수정
                .andExpect(jsonPath("$.code").value("ROOM_001")) // 또는 "ROOM_NOT_FOUND"
                .andExpect(jsonPath("$.message").value("존재하지 않는 방입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
