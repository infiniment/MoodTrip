package com.moodTrip.spring.domain.attraction.controller;

import com.moodTrip.spring.config.WithMockMyUserDetails;
import com.moodTrip.spring.domain.attraction.service.WishlistService;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistApiController.class)
class WishlistApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishlistService wishlistService;

    // 전역 Advice 의존성 해결용 Mock Bean (컨텍스트 로딩을 위해 필요)
    @MockitoBean
    private SecurityUtil securityUtil;
    @MockitoBean
    private EmotionService emotionService;

    @Nested
    @DisplayName("찜 목록에서 삭제 (DELETE /api/wishlist/{attractionId})")
    class RemoveWishlistTest {

        private final Long hardcodedMemberPk = 1L; // 컨트롤러의 getCurrentUserMemberPk()가 반환하는 값
        private final Long testAttractionId = 100L;

        @Test
        // ✅ 해결: @WithMockMyUserDetails 어노테이션을 추가하여 인증된 사용자 요청으로 만듭니다.
        @WithMockMyUserDetails(memberPk = 1L)
        @DisplayName("성공 - 인증된 사용자가 요청 시 200 OK와 성공 메시지를 반환한다")
        void removeWishlist_Success() throws Exception {
            // given
            willDoNothing().given(wishlistService).removeWishlist(hardcodedMemberPk, testAttractionId);

            // when & then
            mockMvc.perform(delete("/api/wishlist/{attractionId}", testAttractionId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("찜이 정상적으로 취소되었습니다."));

            then(wishlistService).should(times(1)).removeWishlist(hardcodedMemberPk, testAttractionId);
        }
        // ✅ 해결: @WithMockMyUserDetails 어노테이션 추가
        @Test
        @WithMockMyUserDetails(memberPk = 1L)
        @DisplayName("실패 - 서비스 계층에서 예외 발생 시 500 에러와 에러 메시지를 반환한다")
        void removeWishlist_Fail_WhenServiceThrowsException() throws Exception {
            // given
            // wishlistService.removeWishlist가 호출되면 RuntimeException을 던지도록 설정
            willThrow(new RuntimeException("DB 오류 또는 기타 예외"))
                    .given(wishlistService).removeWishlist(hardcodedMemberPk, testAttractionId);

            // when & then
            mockMvc.perform(delete("/api/wishlist/{attractionId}", testAttractionId)
                            .with(csrf()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("찜 취소 중 오류가 발생했습니다."));

            // wishlistService.removeWishlist가 1회 호출되었는지 검증
            then(wishlistService).should(times(1)).removeWishlist(hardcodedMemberPk, testAttractionId);
        }

    }
}
