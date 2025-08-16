package com.moodTrip.spring.global.common.util;

import com.moodTrip.spring.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalMemberAdvice {

    private final SecurityUtil securityUtil;

    @ModelAttribute
    public void addMemberInfo(Model model) {
        if (securityUtil.isAuthenticated()) {
            Member member = securityUtil.getCurrentMember();

            String loginType;
            if (member.getProvider() == null || member.getProvider().isBlank()) {
                loginType = "NORMAL";
            } else {
                loginType = member.getProvider().toUpperCase();
            }

            // 🔹 이미지 경로 결정
            String kakaoImagePath = "/image/mypage/kakao-logo-disabled.png";
            String googleImagePath = (loginType.equals("GOOGLE"))
                    ? "/image/mypage/google-logo.png"
                    : "/image/mypage/google-logo-disabled.png";

            model.addAttribute("currentMember", member);
            model.addAttribute("userNickname", member.getNickname());
            model.addAttribute("userEmail", member.getEmail());
            model.addAttribute("loginType", loginType);
            model.addAttribute("kakaoImagePath", kakaoImagePath);
            model.addAttribute("googleImagePath", googleImagePath);
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("isLoggedIn", false);
        }
    }
}
