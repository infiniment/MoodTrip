package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ProfileViewController {

    private final ProfileService profileService;

    @GetMapping("/mypage/my-profile")
    public String viewMyProfile(Model model) {
        log.info("SSR 프로필 페이지 요청");

        try {
            Member testMember = createTestMember();
            ProfileResponse profile = profileService.getMyProfile(testMember);
            model.addAttribute("profile", profile);
            return "mypage/my-profile";
        } catch (Exception e) {
            log.error("SSR 프로필 조회 실패: {}", e.getMessage());

            // 🔥 에러 페이지 대신 기본값으로 처리
            model.addAttribute("error", "프로필을 불러올 수 없습니다.");
            return "mypage/my-profile"; // 같은 페이지에서 에러 메시지 표시
        }
    }

    private Member createTestMember() {
        return Member.builder()
                .memberPk(1L)
                .memberId("testuser123")
                .nickname("테스트유저")
                .email("test@moodtrip.com")
                .memberPhone("010-1234-5678")
                .memberAuth("U")
                .isWithdraw(false)
                .build();
    }
}
