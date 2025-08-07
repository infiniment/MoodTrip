package com.moodTrip.spring.domain.mypageRoom.controller;

import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.ProfileService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 마이페이지 뷰 렌더링 해주는 컨트롤러
@Slf4j
@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageViewController {

    private final SecurityUtil securityUtil;
    private final ProfileService profileService;

    // 기본정보 프로필 페이지
    @GetMapping("/my-profile")
    public String viewMyProfile(Model model) {
        log.info("👤 마이페이지 내 프로필 페이지 요청");

        try {
            Member currentMember = securityUtil.getCurrentMember();
            ProfileResponse profile = profileService.getMyProfile(currentMember);

            model.addAttribute("profile", profile);
            model.addAttribute("currentMember", currentMember);
            model.addAttribute("pageTitle", "내 정보");
            model.addAttribute("isLoggedIn", true);

            return "mypage/my-profile";

        } catch (RuntimeException e) {
            log.warn("❌ 프로필 페이지 JWT 인증 실패: {}", e.getMessage());
            return "redirect:/login?error=로그인이+필요합니다&returnUrl=/mypage/my-profile";

        } catch (Exception e) {
            log.error("💥 프로필 조회 중 예상치 못한 오류", e);
            return "error/500";
        }
    }

    // 마이페이지에 매칭 정보 페이지
    @GetMapping("/my-matching")
    public String myMatching(
            @RequestParam(value = "tab", defaultValue = "received") String tab,
            Model model
    ) {
        log.info("🏠 마이페이지 매칭 정보 페이지 요청 - 탭: {}", tab);

        try {
            Member currentMember = securityUtil.getCurrentMember();

            log.info("👤 매칭 정보 페이지 - 회원ID: {}, 닉네임: {}",
                    currentMember.getMemberId(), currentMember.getNickname());

            model.addAttribute("activeTab", tab);
            model.addAttribute("pageTitle", "매칭 정보");
            model.addAttribute("currentMember", currentMember);
            model.addAttribute("isLoggedIn", true);

            return "mypage/my-matching";

        } catch (RuntimeException e) {
            log.warn("❌ 매칭 정보 페이지 JWT 인증 실패: {}", e.getMessage());
            return "redirect:/login?error=로그인이+필요합니다&returnUrl=/mypage/my-matching";

        } catch (Exception e) {
            log.error("💥 매칭 정보 페이지 로드 실패", e);
            return "error/500";
        }
    }

    //마이페이지의 메인으로 리다이렉트
    @GetMapping
    public String mypageMain() {
        log.info("🏠 마이페이지 메인 요청 - 내 프로필로 리다이렉트");
        return "redirect:/mypage/my-profile";
    }

}
