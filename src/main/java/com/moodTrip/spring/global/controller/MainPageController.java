package com.moodTrip.spring.global.controller;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 🌟 메인 페이지 및 인증 관련 페이지를 담당하는 컨트롤러
 *
 * ✅ 기능:
 * - 메인페이지 렌더링 (로그인 상태별 조건부 헤더)
 * - 로그인/회원가입 페이지 렌더링
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class MainPageController {

    private final SecurityUtil securityUtil;
    private final ProfileRepository profileRepository;

    /**
     * 🏠 메인 페이지 렌더링
     * URL: http://localhost:8080/
     *
     * ✅ 로그인 상태 체크 후 적절한 헤더 렌더링
     * ✅ 사용자 정보를 템플릿에 전달
     */
    @GetMapping("/")
    public String mainPage(Model model) {
        log.info("🏠 메인 페이지 접속");

        if (securityUtil.isAuthenticated()) {
            Member currentMember = securityUtil.getCurrentMember();

            model.addAttribute("isLoggedIn", true);
            model.addAttribute("currentMember", currentMember);
            model.addAttribute("userNickname", currentMember.getNickname());
            model.addAttribute("userEmail", currentMember.getEmail());

            profileRepository.findByMember(currentMember).ifPresent(profile -> {
                model.addAttribute("profileImage", profile.getProfileImage());
            });

            log.info("✅ 로그인 사용자: {}", currentMember.getMemberId());

        } else {
            model.addAttribute("isLoggedIn", false);
            log.info("❌ 비로그인 사용자");
        }

        return "mainpage/mainpage";  // Thymeleaf 템플릿 위치
    }
}
