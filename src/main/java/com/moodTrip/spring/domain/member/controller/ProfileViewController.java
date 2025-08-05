package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.ProfileService;
import com.moodTrip.spring.global.common.util.SecurityUtil; // 🔥 새로 추가!
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ✅ SSR 프로필 페이지 컨트롤러 - JWT 인증 적용
 *
 * 주요 변경사항:
 * - createTestMember() 제거 ❌
 * - SecurityUtil.getCurrentMember() 사용 ✅
 * - 실제 로그인한 사용자의 프로필 페이지 렌더링 ✅
 * - 로그인하지 않은 경우 로그인 페이지로 리다이렉트 ✅
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ProfileViewController {

    private final ProfileService profileService;
    private final SecurityUtil securityUtil; // 🔥 SecurityUtil 주입!

    @GetMapping("/mypage/my-profile")
    public String viewMyProfile(Model model) {
        log.info("SSR 프로필 페이지 요청 - JWT 인증 사용");

        try {
            // 🔥 변경: 더미 데이터 대신 실제 로그인한 사용자 정보 사용
            Member currentMember = securityUtil.getCurrentMember();

            log.info("📝 SSR 프로필 페이지 요청 - 회원ID: {}, 닉네임: {}",
                    currentMember.getMemberId(), currentMember.getNickname());

            // 프로필 정보 조회
            ProfileResponse profile = profileService.getMyProfile(currentMember);

            // 🔥 추가: 사용자 정보를 템플릿에 전달
            model.addAttribute("profile", profile);
            model.addAttribute("currentMember", currentMember); // Member 정보도 추가로 전달
            model.addAttribute("isLoggedIn", true); // 로그인 상태 플래그

            log.info("✅ SSR 프로필 페이지 렌더링 성공 - 회원ID: {}", currentMember.getMemberId());

            return "mypage/my-profile"; // 템플릿 경로

        } catch (RuntimeException e) {
            log.warn("❌ JWT 인증 실패 또는 프로필 조회 실패: {}", e.getMessage());

            // 🔥 인증 실패 시 로그인 페이지로 리다이렉트
            if (e.getMessage().contains("로그인이 필요") ||
                    e.getMessage().contains("인증") ||
                    e.getMessage().contains("토큰")) {

                log.info("🔄 로그인이 필요함 - 로그인 페이지로 리다이렉트");
                return "redirect:/login?error=로그인이+필요합니다&returnUrl=/mypage/my-profile";
            }

            // 🔥 프로필 조회 실패 시 에러 페이지에 메시지 전달
            model.addAttribute("error", "프로필을 불러올 수 없습니다: " + e.getMessage());
            model.addAttribute("isLoggedIn", false);

            return "mypage/my-profile"; // 에러 메시지와 함께 같은 페이지 렌더링

        } catch (Exception e) {
            log.error("💥 SSR 프로필 조회 중 예상치 못한 오류", e);

            // 🔥 예상치 못한 오류 시 에러 페이지 또는 메인 페이지로 리다이렉트
            model.addAttribute("error", "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            model.addAttribute("isLoggedIn", false);

            return "error/500"; // 500 에러 페이지 또는 "mypage/my-profile"
        }
    }

    @GetMapping("/mypage/edit-profile")
    public String editMyProfile(Model model) {
        log.info("🌐 SSR 프로필 편집 페이지 요청");

        try {
            Member currentMember = securityUtil.getCurrentMember();
            ProfileResponse profile = profileService.getMyProfile(currentMember);

            model.addAttribute("profile", profile);
            model.addAttribute("currentMember", currentMember);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("editMode", true); // 편집 모드 플래그

            log.info("✅ SSR 프로필 편집 페이지 렌더링 성공 - 회원ID: {}", currentMember.getMemberId());

            return "mypage/edit-profile"; // 편집 전용 템플릿

        } catch (RuntimeException e) {
            log.warn("❌ 프로필 편집 페이지 접근 실패: {}", e.getMessage());

            if (e.getMessage().contains("로그인이 필요") ||
                    e.getMessage().contains("인증") ||
                    e.getMessage().contains("토큰")) {
                return "redirect:/login?error=로그인이+필요합니다&returnUrl=/mypage/edit-profile";
            }

            return "redirect:/mypage/my-profile?error=프로필+편집+페이지에+접근할+수+없습니다";

        } catch (Exception e) {
            log.error("💥 프로필 편집 페이지 오류", e);
            return "redirect:/mypage/my-profile?error=시스템+오류가+발생했습니다";
        }
    }

}