package com.moodTrip.spring.domain.member.service;


import com.moodTrip.spring.domain.member.dto.request.MemberRequest;
import com.moodTrip.spring.domain.member.dto.request.NicknameUpdateRequest;
import com.moodTrip.spring.domain.member.dto.response.WithdrawResponse;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.entity.Profile;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.domain.member.repository.ProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private WithdrawDataService withdrawDataService;

    @InjectMocks
    private MemberService memberService;

    // --- 회원가입 (register) 테스트 ---

    @Test
    @DisplayName("신규 회원가입 성공")
    void register_NewMember_Success() {
        // given
        MemberRequest request = createValidMemberRequest();
        when(withdrawDataService.canReactivate(anyString())).thenReturn(false); // 재가입 대상 아님
        when(memberRepository.existsByMemberId(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // when
        memberService.register(request);

        // then
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 중복")
    void register_Fail_DuplicateMemberId() {
        // given
        MemberRequest request = createValidMemberRequest();
        when(withdrawDataService.canReactivate(anyString())).thenReturn(false);
        when(memberRepository.existsByMemberId(request.getUserId())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 아이디입니다.");
    }

    @Test
    @DisplayName("기존 탈퇴 회원 재가입 성공")
    void register_Reactivation_Success() {
        // given
        MemberRequest request = createValidMemberRequest();
        Member reactivatedMember = Member.builder().memberId(request.getUserId()).email("old@email.com").build();
        when(withdrawDataService.canReactivate(request.getUserId())).thenReturn(true);
        when(withdrawDataService.reactivateAccount(request.getUserId())).thenReturn(reactivatedMember);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("newEncodedPassword");

        // when
        memberService.register(request);

        // then
        verify(withdrawDataService).reactivateAccount(request.getUserId());
        verify(memberRepository).save(reactivatedMember);
        assertThat(reactivatedMember.getMemberPw()).isEqualTo("newEncodedPassword");
    }


    // --- 닉네임 수정 (updateNickname) 테스트 ---

    @Test
    @DisplayName("닉네임 수정 성공")
    void updateNickname_Success() {
        // given
        Member member = Member.builder().memberId("user1").nickname("oldNick").build();
        Profile profile = Profile.builder().member(member).build();
        NicknameUpdateRequest request = new NicknameUpdateRequest("newNick");

        when(profileRepository.findByMember(member)).thenReturn(Optional.of(profile));

        // when
        memberService.updateNickname(member, request);

        // then
        assertThat(member.getNickname()).isEqualTo("newNick");
    }

    @Test
    @DisplayName("닉네임 수정 실패 - 프로필 없음")
    void updateNickname_Fail_ProfileNotFound() {
        // given
        Member member = Member.builder().build();
        NicknameUpdateRequest request = new NicknameUpdateRequest("newNick");
        when(profileRepository.findByMember(member)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.updateNickname(member, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("프로필을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("닉네임 수정 실패 - 유효하지 않은 닉네임 (특수문자)")
    void updateNickname_Fail_InvalidNickname() {
        // given
        Member member = Member.builder().build();
        NicknameUpdateRequest request = new NicknameUpdateRequest("invalid!nick");

        // when & then
        assertThatThrownBy(() -> memberService.updateNickname(member, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("닉네임은 한글, 영문, 숫자만 사용 가능합니다.");
    }

    // --- 회원 탈퇴 (withdrawMember) 테스트 ---

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdrawMember_Success() {
        // given
        Member member = Member.builder().isWithdraw(false).build();
        WithdrawResponse mockResponse = WithdrawResponse.builder().success(true).build();
        when(withdrawDataService.processCompleteWithdraw(member)).thenReturn(mockResponse);

        // when
        WithdrawResponse response = memberService.withdrawMember(member);

        // then
        assertThat(response.isSuccess()).isTrue();
        verify(withdrawDataService).processCompleteWithdraw(member);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 이미 탈퇴한 회원")
    void withdrawMember_Fail_AlreadyWithdrawn() {
        // given
        Member member = Member.builder().isWithdraw(true).build();

        // when & then
        assertThatThrownBy(() -> memberService.withdrawMember(member))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이미 탈퇴 처리된 회원입니다.");
    }

    // 테스트 데이터 생성을 위한 헬퍼 메서드
    private MemberRequest createValidMemberRequest() {
        MemberRequest request = new MemberRequest();
        request.setUserId("newUser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setPasswordConfirm("password123");
        request.setTerms(true);
        return request;
    }
}