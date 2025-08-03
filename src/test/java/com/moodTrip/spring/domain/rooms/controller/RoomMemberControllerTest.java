package com.moodTrip.spring.domain.rooms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.repository.RoomRepository;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.testsupport.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(RoomMemberController.class)
public class RoomMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private RoomRepository roomRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Room mockRoom() {
        return Room.builder()
                .roomId(1L)
                .roomName("테스트방")
                .roomDescription("테스트 설명")
                .roomMaxCount(5)
                .roomCurrentCount(2)
                .build();
    }

    @WithMockCustomUser(username = "gumin", memberPk = 100L, nickname = "구민")
    @Test
    @DisplayName("방 참여 성공")
    void joinRoom_success() throws Exception {
        Room room = mockRoom();

        // given: 해당 roomId에 대한 Room 반환
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        // given: 아직 참여하지 않은 경우
        when(roomService.isMemberInRoom(any(Member.class), eq(room))).thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/v1/room-members/1/join")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "gumin", memberPk = 100L, nickname = "구민")
    @Test
    @DisplayName("방 참여 실패 - 이미 참여한 사용자")
    void joinRoom_alreadyJoined() throws Exception {
        Room room = mockRoom();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomService.isMemberInRoom(any(Member.class), eq(room))).thenReturn(true);

        mockMvc.perform(post("/api/v1/room-members/1/join")
                        .with(csrf()))
                .andExpect(status().isConflict()); // 409
    }

    @WithMockCustomUser(username = "gumin", memberPk = 100L, nickname = "구민")
    @Test
    @DisplayName("방 참여 실패 - 존재하지 않는 방")
    void joinRoom_notFound() throws Exception {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/room-members/1/join")
                        .with(csrf()))
                .andExpect(status().isNotFound()); // 404
    }

    @WithMockCustomUser(username = "gumin", memberPk = 100L, nickname = "구민")
    @Test
    @DisplayName("방 나가기 성공")
    void leaveRoom_success() throws Exception {
        Room room = mockRoom();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        doNothing().when(roomService).leaveRoom(any(Member.class), eq(room));

        mockMvc.perform(delete("/api/v1/room-members/1/leave")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @WithMockCustomUser(username = "gumin", memberPk = 100L, nickname = "구민")
    @Test
    @DisplayName("방 나가기 실패 - 방을 찾을 수 없음")
    void leaveRoom_fail_roomNotFound() throws Exception {
        // given
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(delete("/api/v1/room-members/999/leave")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }


    @WithMockCustomUser(username = "gumin", memberPk = 100L, nickname = "구민")
    @Test
    @DisplayName("방 참여자 목록 조회 성공")
    void getActiveMembers_success() throws Exception {
        Room room = mockRoom();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomService.getActiveMembers(room)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/room-members/1/members")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "gumin", memberPk = 100L, nickname = "구민")
    @Test
    @DisplayName("참여자 목록 조회 실패 - 방이 존재하지 않음")
    void getActiveMembers_fail_roomNotFound() throws Exception {
        // given
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/api/v1/room-members/999/members"))
                .andExpect(status().isNotFound());
    }
}
