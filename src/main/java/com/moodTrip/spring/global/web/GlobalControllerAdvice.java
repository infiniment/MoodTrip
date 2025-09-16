package com.moodTrip.spring.global.web;

import com.moodTrip.spring.domain.emotion.dto.response.EmotionCategoryDto;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.domain.member.service.ProfileService;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final EmotionService emotionService;
    private final ProfileService profileService;

    /**
     * 헤더 감정 카테고리 공통 주입
     */
    @ModelAttribute("headerEmotionCategories")
    public List<EmotionCategoryDto> addHeaderEmotionCategoriesToModel() {
        return emotionService.getEmotionCategories();
    }

    /**
     * 로그인된 사용자의 프로필 이미지 공통 주입
     */
    @ModelAttribute("profileImage")
    public String addProfileImageToModel(@AuthenticationPrincipal MyUserDetails userDetails) {
        if (userDetails == null) return null;

        Long memberPk = userDetails.getMember().getMemberPk();
        // ProfileService 활용
        return profileService.getProfileByMemberId(memberPk).getProfileImage();
    }
}
