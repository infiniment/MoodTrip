package com.moodTrip.spring.global.controller;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainPageController {

    private final SecurityUtil securityUtil;
    private final ProfileRepository profileRepository;

    @GetMapping("/")
    public String mainPage(Model model) {

        boolean loggedIn = false;

        if (securityUtil.isAuthenticated()) {
            Member currentMember = securityUtil.getCurrentMember();
            if (currentMember != null) {
                loggedIn = true;
                model.addAttribute("currentMember", currentMember);
                model.addAttribute("userNickname", currentMember.getNickname());
                model.addAttribute("userEmail", currentMember.getEmail());
                profileRepository.findByMember(currentMember)
                        .ifPresent(profile -> model.addAttribute("profileImage", profile.getProfileImage()));
            }
        }

        model.addAttribute("isLoggedIn", loggedIn);

        return "mainpage/mainpage";
    }

}
