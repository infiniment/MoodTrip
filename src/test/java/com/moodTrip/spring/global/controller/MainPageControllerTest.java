package com.moodTrip.spring.global.controller;

import com.moodTrip.spring.domain.enteringRoom.service.JoinRequestManagementService;
import com.moodTrip.spring.domain.mainpage.dto.response.MainPageRoomResponse;
import com.moodTrip.spring.domain.mainpage.service.MainPageService;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import com.moodTrip.spring.domain.rooms.dto.response.RoomResponse;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.domain.weather.dto.response.MainPageWeatherAttractionResponse;
import com.moodTrip.spring.domain.weather.service.WeatherAttractionService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MainPageControllerTest {

    @Mock private SecurityUtil securityUtil;
    @Mock private ProfileRepository profileRepository;
    @Mock private RoomService roomService;
    @Mock private JoinRequestManagementService joinRequestManagementService;
    @Mock private MainPageService mainPageService;
    @Mock private WeatherAttractionService weatherAttractionService;

    @InjectMocks
    private MainPageController mainPageController;

    private MockMvc mockMvc;

    private Member createTestMember() {
        return Member.builder()
                .memberPk(1L)
                .nickname("테스트유저")
                .email("test@example.com")
                .build();
    }

    private void initMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(mainPageController).build();
    }

    @Test
    @DisplayName("메인 페이지 - 로그인 사용자")
    void mainPage_LoggedIn() throws Exception {
        initMockMvc();
        Member testMember = createTestMember();

        // given
        given(securityUtil.isAuthenticated()).willReturn(true);
        given(securityUtil.getCurrentMember()).willReturn(testMember);
        given(profileRepository.findByMember(testMember)).willReturn(Optional.empty());
        given(joinRequestManagementService.getTotalPendingRequestsForSidebar()).willReturn(3);

        given(mainPageService.getPopularRooms()).willReturn(List.of(
                MainPageRoomResponse.builder().roomId(1L).roomName("방1").build(),
                MainPageRoomResponse.builder().roomId(2L).roomName("방2").build()
        ));

        given(weatherAttractionService.getMainPageWeatherRecommendations()).willReturn(List.of(
                MainPageWeatherAttractionResponse.builder().attractionId(10L).attractionName("날씨추천1").build()
        ));

        given(roomService.getAllRooms()).willReturn(List.of(
                RoomResponse.builder().roomId(1L).roomName("방A").build()
        ));

        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("mainpage/mainpage"))
                .andExpect(model().attributeExists("isLoggedIn"))
                .andExpect(model().attribute("isLoggedIn", true))
                .andExpect(model().attribute("userNickname", "테스트유저"))
                .andExpect(model().attribute("userEmail", "test@example.com"))
                .andExpect(model().attribute("totalPendingRequests", 3))
                .andExpect(model().attributeExists("popularRooms"))
                .andExpect(model().attributeExists("weatherRecommendations"))
                .andExpect(model().attributeExists("rooms"));
    }

    @Test
    @DisplayName("메인 페이지 - 비로그인 사용자")
    void mainPage_NotLoggedIn() throws Exception {
        initMockMvc();

        // given
        given(securityUtil.isAuthenticated()).willReturn(false);
        given(mainPageService.getPopularRooms()).willReturn(List.of());
        given(weatherAttractionService.getMainPageWeatherRecommendations()).willReturn(List.of());
        given(roomService.getAllRooms()).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("mainpage/mainpage"))
                .andExpect(model().attribute("isLoggedIn", false))
                .andExpect(model().attribute("popularRooms", List.of()))
                .andExpect(model().attribute("weatherRecommendations", List.of()))
                .andExpect(model().attribute("rooms", List.of()));
    }
}
