package com.moodTrip.spring.domain.rooms.controller;

import com.moodTrip.spring.domain.emotion.dto.response.EmotionCategoryDto;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.domain.rooms.dto.response.RoomCardDto;
import com.moodTrip.spring.domain.rooms.service.RoomService;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.global.common.util.GlobalMemberAdvice;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import com.moodTrip.spring.global.web.GlobalControllerAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 통합 테스트:
 * - 실제 스프링 컨텍스트 로딩
 * - MVC 라우팅/뷰리졸버/모델 바인딩/리다이렉트 검증
 * - 서비스 레이어는 MockBean 주입으로 대체
 */


@SpringBootTest
@AutoConfigureMockMvc
class RoomViewControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean(name = "attractionServiceImpl")
    private com.moodTrip.spring.domain.attraction.service.AttractionServiceImpl attractionService;

    @MockitoBean
    private EmotionService emotionService;

    @MockitoBean
    GlobalMemberAdvice globalMemberAdvice;

    @MockitoBean
    GlobalControllerAdvice globalControllerAdvice;

    static class DummyMember {
        private final String email;
        private final String nickname;
        DummyMember(String email, String nickname) {
            this.email = email;
            this.nickname = nickname;
        }
        public String getEmail() { return email; }
        public String getNickname() { return nickname; }
    }

    @BeforeEach
    void setUpAdvice() {
        org.mockito.Mockito.doAnswer(inv -> {
                    org.springframework.ui.Model model = inv.getArgument(0);
                    // ✅ Map 대신 자바빈 객체로 넣기 (email, nickname 모두 지원)
                    model.addAttribute("currentMember", new DummyMember("tester@example.com", "테스터"));
                    model.addAttribute("isLoggedIn", true);
                    model.addAttribute("headerEmotionCategories", java.util.Collections.emptyList());
                    return null;
                }).when(globalMemberAdvice)
                .addMemberInfo(org.mockito.ArgumentMatchers.any(org.springframework.ui.Model.class));

        // (있는 경우만) 파라미터 없는 헤더 메서드 막기
        // doNothing().when(globalControllerAdvice).addHeaderEmotionCategoriesToModel();
    }

    private Authentication authWith(MyUserDetails principal) {
        return new UsernamePasswordAuthenticationToken(principal, null, principal == null ? Collections.emptyList() : principal.getAuthorities());
    }

    private Authentication authWithMockMyUser() {
        MyUserDetails mockPrincipal = mock(MyUserDetails.class);
        when(mockPrincipal.getAuthorities()).thenReturn(Collections.emptyList());
        when(mockPrincipal.getUsername()).thenReturn("tester");
        return authWith(mockPrincipal);
    }


    // --- /start
    @Test
    @DisplayName("GET /companion-rooms/start -> 시작 뷰 렌더링")
    void showStartPage() throws Exception {
        mockMvc.perform(get("/companion-rooms/start"))
                .andExpect(status().isOk())
                .andExpect(view().name("creatingRoom/creatingRoom-start"));
    }

    // --- /list
    @Test
    @DisplayName("GET /companion-rooms/list (keyword 없음) -> 전체 목록 호출 및 모델 바인딩")
    void showListPage_withoutKeyword() throws Exception {
        when(roomService.getRoomCards()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/companion-rooms/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("enteringRoom/enteringRoom"))
                .andExpect(model().attribute("rooms", hasSize(0)))
                .andExpect(model().attribute("currentSearch", nullValue()))
                .andExpect(model().attribute("totalCount", is(0)));

        verify(roomService, times(1)).getRoomCards();
        verify(roomService, never()).searchRooms(any(), anyString());
    }

    @Test
    @DisplayName("GET /companion-rooms/list?keyword=jeju -> 검색 호출 및 모델 바인딩")
    void showListPage_withKeyword() throws Exception {
        List<RoomCardDto> fake = Collections.emptyList();
        when(roomService.searchRooms(isNull(), eq("jeju"))).thenReturn(fake);

        mockMvc.perform(get("/companion-rooms/list").param("keyword", "jeju"))
                .andExpect(status().isOk())
                .andExpect(view().name("enteringRoom/enteringRoom"))
                .andExpect(model().attribute("rooms", hasSize(0)))
                .andExpect(model().attribute("currentSearch", is("jeju")))
                .andExpect(model().attribute("totalCount", is(0)));

        verify(roomService, times(1)).searchRooms(isNull(), eq("jeju"));
        verify(roomService, never()).getRoomCards();
    }

    // --- /create
    @Nested
    class CreatePage {
        @Test
        @DisplayName("GET /companion-rooms/create (비로그인) -> 로그인 리다이렉트")
        void create_unauthenticated_redirects() throws Exception {
            mockMvc.perform(get("/companion-rooms/create").param("new", "true"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/login?redirect=*"));
        }

        @Test
        @DisplayName("GET /companion-rooms/create (로그인) -> 상세 생성 뷰")
        void create_authenticated_ok() throws Exception {
            mockMvc.perform(get("/companion-rooms/create").param("new", "false")
                            .with(authentication(authWithMockMyUser())))
                    .andExpect(status().isOk())
                    .andExpect(view().name("creatingRoom/creatingRoom-detail"));
        }
    }

    // --- /emotion
    @Nested
    class EmotionPage {
        @Test
        @DisplayName("GET /companion-rooms/emotion (비로그인) -> 로그인 리다이렉트")
        void emotion_unauthenticated_redirects() throws Exception {
            mockMvc.perform(get("/companion-rooms/emotion"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/login?redirect=*"));
        }

        @Test
        @DisplayName("GET /companion-rooms/emotion (로그인) -> 감정 카테고리 모델 바인딩 및 뷰")
        void emotion_authenticated_ok() throws Exception {
            when(emotionService.getEmotionCategories()).thenReturn(Collections.<EmotionCategoryDto>emptyList());

            mockMvc.perform(get("/companion-rooms/emotion")
                            .with(authentication(authWithMockMyUser())))
                    .andExpect(status().isOk())
                    .andExpect(view().name("creatingRoom/choosing-emotion"))
                    .andExpect(model().attributeExists("emotionCategories"))
                    .andExpect(model().attribute("emotionCategories", hasSize(0)));

            verify(emotionService, times(1)).getEmotionCategories();
        }
    }

    // --- /attraction
    @Nested
    class AttractionPage {
        @Test
        @DisplayName("GET /companion-rooms/attraction (비로그인) -> 파라미터 포함 로그인 리다이렉트")
        void attraction_unauthenticated_redirects() throws Exception {
            mockMvc.perform(get("/companion-rooms/attraction")
                            .param("q", "한강")
                            .param("areaCode", "1")
                            .param("sigunguCode", "2")
                            .param("contentTypeId", "12")
                            .param("page", "0")
                            .param("size", "9"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/login?redirect=*%2Fcompanion-rooms%2Fattraction*"));
        }

        @Test
        @DisplayName("GET /companion-rooms/attraction (로그인) -> 검색 호출/페이지네이션 모델 바인딩/뷰")
        void attraction_authenticated_ok() throws Exception {
            Page<Object> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 9), 0);
            when(attractionService.searchKeywordPrefTitleStarts(
                    any(), any(), any(), any(), anyInt(), anyInt())
            ).thenReturn((Page) emptyPage);

            mockMvc.perform(get("/companion-rooms/attraction")
                            .param("q", "서울")
                            .param("page", "0")
                            .param("size", "9")
                            .with(authentication(authWithMockMyUser())))
                    .andExpect(status().isOk())
                    .andExpect(view().name("creatingRoom/choosing-attraction"))
                    .andExpect(model().attribute("isLoggedIn", is(true)))
                    .andExpect(model().attribute("q", is("서울")))
                    .andExpect(model().attribute("attractions", hasSize(0)))
                    .andExpect(model().attribute("page", is(0)))
                    .andExpect(model().attribute("size", is(9)))
                    .andExpect(model().attribute("totalPages", is(0)))
                    .andExpect(model().attribute("totalElements", equalTo(0L)));

            verify(attractionService, times(1)).searchKeywordPrefTitleStarts(
                    eq("서울"), isNull(), isNull(), isNull(), eq(0), eq(9));
        }
    }

    // --- /schedule
    @Nested
    class SchedulePage {
        @Test
        @DisplayName("GET /companion-rooms/schedule (비로그인) -> 로그인 리다이렉트")
        void schedule_unauthenticated_redirects() throws Exception {
            mockMvc.perform(get("/companion-rooms/schedule"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/login?redirect=*"));
        }

        @Test
        @DisplayName("GET /companion-rooms/schedule (로그인) -> 스케줄 선택 뷰")
        void schedule_authenticated_ok() throws Exception {
            mockMvc.perform(get("/companion-rooms/schedule")
                            .with(authentication(authWithMockMyUser())))
                    .andExpect(status().isOk())
                    .andExpect(view().name("creatingRoom/choosing-schedule"));
        }
    }

    // --- /final
    @Nested
    class FinalPage {
        @Test
        @DisplayName("GET /companion-rooms/final (비로그인) -> 로그인 리다이렉트")
        void final_unauthenticated_redirects() throws Exception {
            mockMvc.perform(get("/companion-rooms/final"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/login?redirect=*"));
        }

        @Test
        @DisplayName("GET /companion-rooms/final (로그인) -> 최종 등록 뷰")
        void final_authenticated_ok() throws Exception {
            mockMvc.perform(get("/companion-rooms/final")
                            .with(authentication(authWithMockMyUser())))
                    .andExpect(status().isOk())
                    .andExpect(view().name("creatingRoom/final-registration"));
        }
    }
}
