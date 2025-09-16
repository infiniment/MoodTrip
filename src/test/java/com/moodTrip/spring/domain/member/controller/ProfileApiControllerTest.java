package com.moodTrip.spring.domain.member.controller;
import com.moodTrip.spring.domain.emotion.service.EmotionService;
import com.moodTrip.spring.domain.member.dto.request.IntroduceUpdateRequest;
import com.moodTrip.spring.domain.member.dto.request.NicknameUpdateRequest;
import com.moodTrip.spring.domain.member.dto.request.ProfileImageUpdateRequest;
import com.moodTrip.spring.domain.member.dto.response.ProfileResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.domain.member.service.ProfileService;
import com.moodTrip.spring.global.common.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProfileApiController.class)
@AutoConfigureMockMvc(addFilters = true)
class ProfileApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private SecurityUtil securityUtil;

    @MockBean
    private EmotionService emotionService;

    // 공통 로그인된 사용자 생성
    private Member createMockMember() {
        Member member = Mockito.mock(Member.class);
        Mockito.when(member.getMemberId()).thenReturn("mockUser");
        Mockito.when(member.getNickname()).thenReturn("mockNick");
        return member;
    }
    @Test
    @WithMockUser // 시큐리티 인증 모의
    @DisplayName("내 프로필 조회 성공")
    void getMyProfile_Success() throws Exception {
        Member currentMember = createMockMember();
        when(securityUtil.getCurrentMember()).thenReturn(currentMember);
        ProfileResponse response = ProfileResponse.builder().nickname("mockNick").build();
        when(profileService.getMyProfile(currentMember)).thenReturn(response);

        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("mockNick"));
    }

    @Test
    @WithMockUser
    @DisplayName("내 프로필 조회 - 인증 오류 시 400 반환")
    void getMyProfile_AuthError() throws Exception {
        when(securityUtil.getCurrentMember()).thenThrow(new RuntimeException("인증 실패"));

        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void updateNickname_Success() throws Exception {
        Member currentMember = createMockMember();
        when(securityUtil.getCurrentMember()).thenReturn(currentMember);

        NicknameUpdateRequest req = new NicknameUpdateRequest("newNick");
        ProfileResponse updatedProfile = ProfileResponse.builder()
                .nickname("newNick")
                .build();

        when(memberService.updateNickname(any(Member.class), any(NicknameUpdateRequest.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(patch("/api/v1/profiles/me/nickname")
                        .with(csrf())  // CSRF 토큰 추가 부분
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("newNick"));
    }

//    @Test
//    @WithMockUser
//    @DisplayName("닉네임 수정 실패 - 잘못된 닉네임으로 400 반환")
//    void updateNickname_Fail_BadRequest() throws Exception {
//        when(securityUtil.getCurrentMember()).thenReturn(createMockMember());
//        when(memberService.updateNickname(any(Member.class), any(NicknameUpdateRequest.class)));
//        doThrow(new RuntimeException("잘못된 닉네임"))
//                .when(memberService).updateNickname(any(Member.class), any(NicknameUpdateRequest.class));
//
//
//        NicknameUpdateRequest req = new NicknameUpdateRequest("bad@nick");
//
//        mockMvc.perform(patch("/api/v1/profiles/me/nickname")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andExpect(status().isBadRequest());
//    }

    @Test
    @WithMockUser
    @DisplayName("자기소개 수정 성공")
    void updateIntroduce_Success() throws Exception {
        Member currentMember = createMockMember();
        when(securityUtil.getCurrentMember()).thenReturn(currentMember);
        IntroduceUpdateRequest req = new IntroduceUpdateRequest("This is my bio");
        ProfileResponse updatedProfile = ProfileResponse.builder().profileBio("This is my bio").build();
        when(profileService.updateIntroduce(any(Member.class), any(IntroduceUpdateRequest.class))).thenReturn(updatedProfile);

        mockMvc.perform(patch("/api/v1/profiles/me/introduce")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileBio").value("This is my bio"));
    }

    @Test
    @WithMockUser
    @DisplayName("프로필 사진 변경 성공")
    void updateProfileImage_Success() throws Exception {
        Member currentMember = createMockMember();
        when(securityUtil.getCurrentMember()).thenReturn(currentMember);

        ProfileImageUpdateRequest req = new ProfileImageUpdateRequest("/images/new-image.png");
        ProfileResponse updatedProfile = ProfileResponse.builder().profileImage("/images/new-image.png").build();

        when(profileService.updateProfileImage(any(Member.class), any(ProfileImageUpdateRequest.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(patch("/api/v1/profiles/me/profileImage")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImage").value("/images/new-image.png"));
    }
}