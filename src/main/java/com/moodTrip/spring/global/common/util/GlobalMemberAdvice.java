    package com.moodTrip.spring.global.common.util;

    import com.moodTrip.spring.domain.member.entity.Member;
    import com.moodTrip.spring.global.common.util.SecurityUtil;
    import lombok.RequiredArgsConstructor;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.ControllerAdvice;
    import org.springframework.web.bind.annotation.ModelAttribute;

    @ControllerAdvice
    @RequiredArgsConstructor
    public class GlobalMemberAdvice {
        // header-after-login에서 사용되는 회원 정보
        private final SecurityUtil securityUtil;

        @ModelAttribute
        public void addMemberInfo(Model model) {
            if (securityUtil.isAuthenticated()) {
                Member member = securityUtil.getCurrentMember();
                model.addAttribute("currentMember", member);
                model.addAttribute("userNickname", member.getNickname());
                model.addAttribute("userEmail", member.getEmail());
                model.addAttribute("isLoggedIn", true);
            } else {
                model.addAttribute("isLoggedIn", false);
            }
        }
    }
