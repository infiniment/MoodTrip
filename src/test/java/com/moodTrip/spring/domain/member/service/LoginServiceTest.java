package com.moodTrip.spring.domain.member.service;

import com.moodTrip.spring.domain.member.dto.request.LoginRequest;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.global.security.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    private LoginRequest loginRequest;
    private Member mockMember;

    @BeforeEach
    void setUp() {
        // 테스트에 공통적으로 사용할 요청 객체와 Mock Member 객체를 설정합니다.
        loginRequest = new LoginRequest("testUser", "password123");

        mockMember = Member.builder()
                .memberId("testUser")
                .memberPw("encodedPassword") // DB에는 암호화된 비밀번호가 저장되어 있다고 가정
                .memberPk(1L)
                .build();
    }

    @Test
    @DisplayName("로그인 성공 시 JWT 토큰을 반환한다")
    void login_Success() {
        // given: Repository, PasswordEncoder, JwtUtil이 모두 정상 동작하는 상황을 설정
        when(memberRepository.findByMemberId(loginRequest.getMemberId()))
                .thenReturn(Optional.of(mockMember));

        when(passwordEncoder.matches(loginRequest.getMemberPw(), mockMember.getMemberPw()))
                .thenReturn(true);

        when(jwtUtil.generateToken(mockMember.getMemberId(), mockMember.getMemberPk()))
                .thenReturn("dummy-jwt-token");

        // when: 로그인 서비스 실행
        String token = loginService.login(loginRequest);

        // then: 반환된 토큰이 예상과 같고, 모든 의존성이 올바르게 호출되었는지 검증
        assertThat(token).isEqualTo("dummy-jwt-token");
        verify(memberRepository).findByMemberId(loginRequest.getMemberId());
        verify(passwordEncoder).matches(loginRequest.getMemberPw(), mockMember.getMemberPw());
        verify(jwtUtil).generateToken(mockMember.getMemberId(), mockMember.getMemberPk());
    }

    @Test
    @DisplayName("존재하지 않는 아이디로 로그인 시 null을 반환한다")
    void login_withNonExistentUser_shouldReturnNull() {
        // given: Repository가 비어있는 Optional을 반환하는 상황 설정
        when(memberRepository.findByMemberId(loginRequest.getMemberId()))
                .thenReturn(Optional.empty());

        // when: 로그인 서비스 실행
        String token = loginService.login(loginRequest);

        // then: 반환값이 null인지 확인
        assertThat(token).isNull();
        // 비밀번호 검증이나 토큰 생성 로직이 호출되지 않았는지 검증
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않을 시 null을 반환한다")
    void login_withWrongPassword_shouldReturnNull() {
        // given: Repository는 사용자를 찾았지만, PasswordEncoder가 false를 반환하는 상황 설정
        when(memberRepository.findByMemberId(loginRequest.getMemberId()))
                .thenReturn(Optional.of(mockMember));

        when(passwordEncoder.matches(loginRequest.getMemberPw(), mockMember.getMemberPw()))
                .thenReturn(false);

        // when: 로그인 서비스 실행
        String token = loginService.login(loginRequest);

        // then: 반환값이 null인지 확인
        assertThat(token).isNull();
        // 토큰 생성 로직이 호출되지 않았는지 검증
        verify(jwtUtil, never()).generateToken(any(), any());
    }
}