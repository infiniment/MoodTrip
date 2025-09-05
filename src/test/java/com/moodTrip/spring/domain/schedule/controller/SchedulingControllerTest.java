package com.moodTrip.spring.domain.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.rooms.service.RoomAuthService;
import com.moodTrip.spring.domain.schedule.dto.request.ScheduleRequest;
import com.moodTrip.spring.domain.schedule.dto.response.ScheduleResponse;
import com.moodTrip.spring.domain.schedule.dto.response.ScheduleWebSocketMessage;
import com.moodTrip.spring.domain.schedule.service.ScheduleService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SchedulingController.class)
@AutoConfigureMockMvc(addFilters = false)
class SchedulingControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    @MockitoBean
    ScheduleService scheduleService;
    @MockitoBean RoomAuthService roomAuthService;
    @MockitoBean SimpMessagingTemplate messagingTemplate;

    @MockitoBean com.moodTrip.spring.domain.emotion.service.EmotionService emotionService;

    @MockitoBean
    SecurityUtil securityUtil;

    private void setAuthWithMemberPk(Long memberPk) {
        MyUserDetails mud = mock(MyUserDetails.class);
        Member m = new Member();
        m.setMemberPk(memberPk);
        when(mud.getMember()).thenReturn(m);

        var auth = new UsernamePasswordAuthenticationToken(mud, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(securityUtil.getCurrentMemberPk()).thenReturn(memberPk);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/schedules/room/{roomId} : 일정 생성 + 권한검사 + WS 브로드캐스트")
    void createSchedule_ok() throws Exception {
        long roomId = 10L;
        long memberPk = 100L;
        long scheduleId = 1L;
        setAuthWithMemberPk(memberPk);

        ScheduleRequest req = new ScheduleRequest();
        ScheduleResponse resp = ScheduleResponse.builder()
                .scheduleId(scheduleId)
                .roomId(roomId)
                .build();

        when(scheduleService.createSchedule(eq(roomId), any(ScheduleRequest.class)))
                .thenReturn(resp);

        mockMvc.perform(
                        post("/api/schedules/room/{roomId}", roomId)
                                .contentType("application/json")
                                .content(om.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value((int) scheduleId))
                .andExpect(jsonPath("$.roomId").value((int) roomId));

        verify(roomAuthService).assertActiveMember(roomId, memberPk);

        ArgumentCaptor<String> destCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCap = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(destCap.capture(), payloadCap.capture());

        assertThat(destCap.getValue()).isEqualTo("/sub/schedule/room/" + roomId);

        ScheduleWebSocketMessage msg = (ScheduleWebSocketMessage) payloadCap.getValue();
        assertThat(msg.getType()).isEqualTo("CREATE");
        ScheduleResponse body = (ScheduleResponse) msg.getData();
        assertThat(body.getScheduleId()).isEqualTo(scheduleId);
        assertThat(body.getRoomId()).isEqualTo(roomId);
    }

    @Test
    @DisplayName("GET /api/schedules/room/{roomId} : 일정 목록 조회 + 권한검사")
    void getSchedules_ok() throws Exception {
        long roomId = 20L;
        long memberPk = 200L;
        setAuthWithMemberPk(memberPk);

        var r1 = ScheduleResponse.builder().scheduleId(11L).roomId(roomId).build();
        var r2 = ScheduleResponse.builder().scheduleId(12L).roomId(roomId).build();

        when(scheduleService.getSchedulesByRoomId(roomId)).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/schedules/room/{roomId}", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].scheduleId").value(11))
                .andExpect(jsonPath("$[1].scheduleId").value(12));

        verify(roomAuthService).assertActiveMember(roomId, memberPk);
        verify(scheduleService).getSchedulesByRoomId(roomId);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("PUT /api/schedules/{scheduleId} : 일정 수정 + 방ID 조회 + 권한검사 + WS 브로드캐스트")
    void updateSchedule_ok() throws Exception {
        long scheduleId = 30L;
        long roomId = 300L;
        long memberPk = 3000L;
        setAuthWithMemberPk(memberPk);

        ScheduleRequest req = new ScheduleRequest();
        ScheduleResponse updated = ScheduleResponse.builder()
                .scheduleId(scheduleId)
                .roomId(roomId)
                .build();

        when(scheduleService.getRoomIdByScheduleId(scheduleId)).thenReturn(roomId);
        when(scheduleService.updateSchedule(eq(scheduleId), any(ScheduleRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(
                        put("/api/schedules/{scheduleId}", scheduleId)
                                .contentType("application/json")
                                .content(om.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleId").value((int) scheduleId))
                .andExpect(jsonPath("$.roomId").value((int) roomId));

        verify(roomAuthService).assertActiveMember(roomId, memberPk);

        ArgumentCaptor<String> destCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCap = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(destCap.capture(), payloadCap.capture());

        assertThat(destCap.getValue()).isEqualTo("/sub/schedule/room/" + roomId);

        ScheduleWebSocketMessage msg = (ScheduleWebSocketMessage) payloadCap.getValue();
        assertThat(msg.getType()).isEqualTo("UPDATE");
        ScheduleResponse body = (ScheduleResponse) msg.getData();
        assertThat(body.getScheduleId()).isEqualTo(scheduleId);
        assertThat(body.getRoomId()).isEqualTo(roomId);
    }

    @Test
    @DisplayName("DELETE /api/schedules/{scheduleId} : 일정 삭제 + 방ID 조회 + 권한검사 + WS 브로드캐스트")
    void deleteSchedule_ok() throws Exception {
        long scheduleId = 40L;
        long roomId = 400L;
        long memberPk = 4000L;
        setAuthWithMemberPk(memberPk);

        when(scheduleService.getRoomIdByScheduleId(scheduleId)).thenReturn(roomId);
        doNothing().when(scheduleService).deleteSchedule(scheduleId);

        mockMvc.perform(delete("/api/schedules/{scheduleId}", scheduleId))
                .andExpect(status().isNoContent());

        verify(roomAuthService).assertActiveMember(roomId, memberPk);
        verify(scheduleService).deleteSchedule(scheduleId);

        ArgumentCaptor<String> destCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCap = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(destCap.capture(), payloadCap.capture());

        assertThat(destCap.getValue()).isEqualTo("/sub/schedule/room/" + roomId);

        ScheduleWebSocketMessage msg = (ScheduleWebSocketMessage) payloadCap.getValue();
        assertThat(msg.getType()).isEqualTo("DELETE");
        assertThat((Long) msg.getData()).isEqualTo(scheduleId);
    }
}
