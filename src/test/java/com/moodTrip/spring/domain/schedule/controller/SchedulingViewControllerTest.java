package com.moodTrip.spring.domain.schedule.controller;

import com.moodTrip.spring.domain.rooms.dto.response.RoomMemberResponse;
import com.moodTrip.spring.domain.rooms.entity.Room;
import com.moodTrip.spring.domain.rooms.service.RoomAuthService;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SchedulingViewController.class)
@AutoConfigureMockMvc(addFilters = false)
class SchedulingViewControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean RoomService roomService;
    @MockitoBean RoomAuthService roomAuthService;

    @MockitoBean com.moodTrip.spring.global.common.util.SecurityUtil securityUtil;

    @MockitoBean com.moodTrip.spring.domain.emotion.service.EmotionService emotionService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    private void authenticate(long memberPk) {
        MyUserDetails mud = Mockito.mock(MyUserDetails.class);
        var member = new com.moodTrip.spring.domain.member.entity.Member();
        member.setMemberPk(memberPk);
        when(mud.getMember()).thenReturn(member);

        var auth = new UsernamePasswordAuthenticationToken(mud, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // ✅ 컨트롤러/어드바이스에서 보안 유틸을 호출하면 정상값 반환되도록
        when(securityUtil.getCurrentMemberPk()).thenReturn(memberPk);
        when(securityUtil.getCurrentMember()).thenReturn(member);
    }

    private Room makeRoom(long roomId, LocalDate start, LocalDate end) {
        Room r = new Room();
        r.setRoomId(roomId);
        r.setTravelStartDate(start);
        r.setTravelEndDate(end);
        // 목적지 좌표(우선순위 1)
        r.setDestinationLat(BigDecimal.valueOf(37.5665));
        r.setDestinationLon(BigDecimal.valueOf(126.9780));
        // attraction이 없는 케이스
        r.setAttraction(null);
        return r;
    }

    @Test
    @DisplayName("미인증 사용자는 /login 으로 리다이렉트")
    void showSchedulingPage_unauthenticated_redirectLogin() throws Exception {
        mockMvc.perform(get("/scheduling/{roomId}", 10L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("방 참여자가 아니면 403 + errors/room-forbidden 뷰")
    void showSchedulingPage_forbidden_whenNotMember() throws Exception {
        long roomId = 10L;
        authenticate(100L);

        when(roomAuthService.isActiveMember(roomId, 100L)).thenReturn(false);

        mockMvc.perform(get("/scheduling/{roomId}", roomId))
                .andExpect(status().isForbidden())
                .andExpect(view().name("errors/room-forbidden"))
                .andExpect(model().attribute("roomId", roomId))
                .andExpect(model().attribute("reason", not(isEmptyOrNullString())));
    }

    @Test
    @DisplayName("정상 접근: 방/멤버/기간/좌표 모델 속성 및 뷰 이름 검증")
    void showSchedulingPage_ok() throws Exception {
        long roomId = 20L;
        long memberPk = 200L;
        authenticate(memberPk);

        when(roomAuthService.isActiveMember(roomId, memberPk)).thenReturn(true);

        // 방 정보
        LocalDate start = LocalDate.of(2025, 9, 10);
        LocalDate end   = LocalDate.of(2025, 9, 12); // 2박 3일
        Room room = makeRoom(roomId, start, end);
        when(roomService.getRoomWithAttraction(roomId)).thenReturn(room);

        // 참여자 목록
        var m1 = RoomMemberResponse.builder().memberPk(1L).nickname("A").build();
        var m2 = RoomMemberResponse.builder().memberPk(2L).nickname("B").build();
        when(roomService.getActiveMembers(any(Room.class))).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/scheduling/{roomId}", roomId))
                .andExpect(status().isOk())
                .andExpect(view().name("schedule-with-companion/scheduling"))
                // 기본 모델 값들
                .andExpect(model().attributeExists("room", "members", "roomId", "durationLabel", "currentMember"))
                .andExpect(model().attribute("roomId", roomId))
                // 기간 라벨: 2박3일
                .andExpect(model().attribute("durationLabel", "(2박3일)"))
                // 좌표는 destinationLat/Lon 우선
                .andExpect(model().attribute("lat", room.getDestinationLat()))
                .andExpect(model().attribute("lon", room.getDestinationLon()))
                // 멤버 2명
                .andExpect(model().attribute("members", hasSize(2)));
    }

    @Test
    @DisplayName("목적지 좌표 없고 attraction 좌표가 있으면 그것을 사용")
    void showSchedulingPage_usesAttractionCoords_whenDestinationNull() throws Exception {
        long roomId = 30L;
        long memberPk = 300L;
        authenticate(memberPk);

        when(roomAuthService.isActiveMember(roomId, memberPk)).thenReturn(true);

        // 목적지 좌표는 null, attraction 좌표 사용
        Room room = new Room();
        room.setRoomId(roomId);
        room.setTravelStartDate(LocalDate.of(2025, 9, 1));
        room.setTravelEndDate(LocalDate.of(2025, 9, 1)); // 당일치기
        room.setDestinationLat(null);
        room.setDestinationLon(null);

        var attraction = new com.moodTrip.spring.domain.attraction.entity.Attraction();
        attraction.setMapY(35.0);
        attraction.setMapX(129.0);
        room.setAttraction(attraction);

        when(roomService.getRoomWithAttraction(roomId)).thenReturn(room);
        when(roomService.getActiveMembers(any(Room.class))).thenReturn(List.of());

        mockMvc.perform(get("/scheduling/{roomId}", roomId))
                .andExpect(status().isOk())
                .andExpect(view().name("schedule-with-companion/scheduling"))
                .andExpect(model().attribute("durationLabel", "당일치기"))
                .andExpect(model().attribute("lat", attraction.getMapY()))
                .andExpect(model().attribute("lon", attraction.getMapX()));
    }
}
