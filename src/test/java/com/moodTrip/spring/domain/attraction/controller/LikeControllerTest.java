package com.moodTrip.spring.domain.attraction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodTrip.spring.config.WithMockMyUserDetails; // 위에서 만든 어노테이션 임포트
import com.moodTrip.spring.domain.attraction.service.LikeService;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LikeController.class)
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LikeService likeService;

    // 전역 Advice 의존성 해결을 위한 Mock Bean
    @MockitoBean
    private SecurityUtil securityUtil;

    @MockitoBean
    private EmotionService emotionService;

    private final Long memberPk = 1L;
    private final Long attractionId = 100L;

    @Nested
    @DisplayName("찜하기(POST /api/likes/{attractionId})")
    class AddLikeTest {

        @Test
        @WithMockMyUserDetails(memberPk = 1L)
        @DisplayName("성공 - 인증된 사용자가 찜하기를 요청하면 200 OK를 반환한다")
        void addLike_Success() throws Exception {
            // given
            willDoNothing().given(likeService).addLike(memberPk, attractionId);

            // when & then
            mockMvc.perform(post("/api/likes/{attractionId}", attractionId)
                            .with(csrf())) // POST 요청 시 CSRF 토큰 추가
                    .andExpect(status().isOk());

            // likeService.addLike가 정확한 인자로 1회 호출되었는지 검증
            then(likeService).should(times(1)).addLike(memberPk, attractionId);
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자가 요청하면 로그인 페이지로 리다이렉트된다")
        void addLike_Fail_RedirectsToLogin() throws Exception {
            mockMvc.perform(post("/api/likes/{attractionId}", attractionId)
                            .with(csrf()))
                    // ✅ 기대값을 401 대신 302(isFound)로 변경
                    .andExpect(status().isFound())
                    // ✅ 리다이렉트 되는 URL이 로그인 페이지인지 검증
                    .andExpect(redirectedUrl("http://localhost/login"));
        }
    }

    @Nested
    @DisplayName("찜 취소하기(DELETE /api/likes/{attractionId})")
    class RemoveLikeTest {

        @Test
        @WithMockMyUserDetails(memberPk = 1L)
        @DisplayName("성공 - 인증된 사용자가 찜 취소를 요청하면 200 OK를 반환한다")
        void removeLike_Success() throws Exception {
            // given
            willDoNothing().given(likeService).removeLike(memberPk, attractionId);

            // when & then
            mockMvc.perform(delete("/api/likes/{attractionId}", attractionId)
                            .with(csrf()))
                    .andExpect(status().isOk());

            then(likeService).should(times(1)).removeLike(memberPk, attractionId);
        }

        @Test
        @DisplayName("실패 - 인증되지 않은 사용자가 요청하면 401 Unauthorized를 반환한다")
        void removeLike_Fail_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/likes/{attractionId}", attractionId)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }


    @Nested
    @DisplayName("찜 상태 조회(GET /api/likes/{attractionId})")
    class IsLikedTest {

        @Test
        @WithMockMyUserDetails(memberPk = 1L)
        @DisplayName("성공 - 로그인 유저가 찜한 상태일 때 liked:true, loggedIn:true를 반환한다")
        void isLiked_Liked() throws Exception {
            // given
            given(likeService.isLiked(memberPk, attractionId)).willReturn(true);

            // when & then
            mockMvc.perform(get("/api/likes/{attractionId}", attractionId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.liked").value(true))
                    .andExpect(jsonPath("$.loggedIn").value(true));
        }

        @Test
        @WithMockMyUserDetails(memberPk = 1L)
        @DisplayName("성공 - 로그인 유저가 찜하지 않은 상태일 때 liked:false, loggedIn:true를 반환한다")
        void isLiked_NotLiked() throws Exception {
            // given
            given(likeService.isLiked(memberPk, attractionId)).willReturn(false);

            // when & then
            mockMvc.perform(get("/api/likes/{attractionId}", attractionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.liked").value(false))
                    .andExpect(jsonPath("$.loggedIn").value(true));
        }


    }
}
