package com.moodTrip.spring.domain.member.controller;

import com.moodTrip.spring.domain.enteringRoom.service.JoinRequestManagementService;
import com.moodTrip.spring.domain.member.dto.request.ChangePasswordForm;
import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.domain.member.service.ProfileService;
import com.moodTrip.spring.global.common.util.SecurityUtil; // 🔥 새로 추가!
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ProfileViewController {

    private final ProfileService profileService;
    private final SecurityUtil securityUtil;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final JoinRequestManagementService joinRequestManagementService;

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

            try {
                Integer totalPendingRequests = joinRequestManagementService.getTotalPendingRequestsForSidebar();
                model.addAttribute("totalPendingRequests", totalPendingRequests);
            } catch (Exception e) {
                log.error("사이드바 배지 데이터 조회 실패", e);
                model.addAttribute("totalPendingRequests", 0);
            }

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

    // GET: 폼 백업 객체 주입
    @GetMapping("/mypage/change-password")
    public String changePasswordPage(Model model) {
        try {
            securityUtil.getCurrentMember(); // 인증 확인


            model.addAttribute("isLoggedIn", true);

            // 플래시에서 넘어온 게 없으면 새 폼 주입
            if (!model.containsAttribute("form")) {
                model.addAttribute("form", new ChangePasswordForm());
            }
            return "mypage/change-password";
        } catch (RuntimeException e) {
            return "redirect:/login?error=로그인이+필요합니다&returnUrl=/mypage/change-password";
        }
    }

    @PostMapping("/mypage/change-password")
    public String processChangePassword(
            @Valid @ModelAttribute("form") ChangePasswordForm form,
            BindingResult bindingResult,
            RedirectAttributes ra
    ) {
        // --- 1. 메서드 진입 로그 ---
        // 가장 먼저 이 로그가 찍히는지 확인하여, 요청이 컨트롤러에 도달하는지 파악합니다.
        log.info("▶▶▶ POST /mypage/change-password - 비밀번호 변경 처리 시작");

        // DTO에 데이터가 잘 담겼는지 확인 (보안상 실제 비밀번호는 로그로 남기지 않는 것이 좋습니다)
        log.info("폼 데이터 수신: currentPassword 입력 여부={}, newPassword 입력 여부={}",
                !form.getCurrentPassword().isBlank(), !form.getNewPassword().isBlank());

        Member currentMember;
        try {
            // --- 2. 사용자 인증 정보 조회 로그 ---
            log.info("인증 정보 확인 시작...");
            currentMember = securityUtil.getCurrentMember();
            log.info("인증된 사용자 확인 완료: ID={}, 닉네임={}", currentMember.getMemberId(), currentMember.getNickname());
        } catch (RuntimeException e) {
            // 만약 이 로그가 찍힌다면, 로그인 세션이 없거나 만료된 것입니다.
            log.warn("🚨 인증된 사용자를 찾을 수 없음! 로그인 페이지로 리다이렉트. 원인: {}", e.getMessage());
            return "redirect:/login?error=세션이+만료되었거나+로그인이+필요합니다.";
        }

        // --- 3. 핵심 검증 로직 로그 ---
        log.info("핵심 검증 로직 시작...");

        // 3-1. 현재 비밀번호 검증
        if (!passwordEncoder.matches(form.getCurrentPassword(), currentMember.getMemberPw())) {
            log.warn("❌ 검증 실패: 현재 비밀번호가 일치하지 않습니다.");
            bindingResult.rejectValue("currentPassword", "password.invalid", "현재 비밀번호가 올바르지 않습니다.");
        }

        // 3-2. 새 비밀번호와 확인 필드 일치 검증
        if (!form.getNewPassword().equals(form.getNewConfirmPassword())) {
            log.warn("❌ 검증 실패: 새 비밀번호가 일치하지 않습니다.");
            bindingResult.rejectValue("newConfirmPassword", "password.mismatch", "새 비밀번호가 일치하지 않습니다.");
        }

        // --- 4. 검증 결과 확인 로그 ---
        // 만약 `bindingResult`에 오류가 있다면, 이 블록이 실행되고 리다이렉트됩니다.
        if (bindingResult.hasErrors()) {
            log.warn("🚨 BindingResult에 검증 오류 발견. 오류 개수: {}", bindingResult.getErrorCount());
            // 어떤 오류가 있는지 상세히 확인
            bindingResult.getAllErrors().forEach(error -> log.warn(" - {}", error.toString()));

            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            ra.addFlashAttribute("form", form);

            log.info("◀◀◀ 리다이렉트: /mypage/change-password (검증 오류 발생)");
            return "redirect:/mypage/change-password";
        }

        // --- 5. 비밀번호 업데이트 로직 진입 로그 ---
        // 만약 위 단계들에서 로그가 찍히지 않고 이 로그도 찍히지 않는다면, 예상치 못한 문제가 있는 것입니다.
        log.info("✅ 모든 검증 통과! 비밀번호 업데이트 로직을 실행합니다.");

        try {
            memberService.updatePassword(currentMember, passwordEncoder.encode(form.getNewPassword()));
            ra.addFlashAttribute("success", true);
            log.info("🎉 비밀번호 업데이트 성공!");
        } catch (Exception e) {
            // DB 업데이트 등 최종 단계에서 에러가 발생하면 이 로그가 찍힙니다.
            log.error("💥 비밀번호 업데이트 중 심각한 오류 발생 - 회원 ID: {}", currentMember.getMemberId(), e);
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("error", "알 수 없는 오류로 비밀번호 변경에 실패했습니다.");
        }

        log.info("◀◀◀ 리다이렉트: /mypage/change-password (처리 완료)");
        return "redirect:/mypage/change-password";
    }



}