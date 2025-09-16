package com.moodTrip.spring.domain.member.service;

import com.moodTrip.spring.domain.member.dto.request.LoginRequest;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import com.moodTrip.spring.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public String login(LoginRequest loginRequest) {


        log.info("로그인 시도: memberId={}", loginRequest.getMemberId());
        Optional<Member> optionalMember = memberRepository.findByMemberId(loginRequest.getMemberId());
        if (optionalMember.isEmpty()) {
            log.warn("로그인 실패: 존재하지 않는 memberId={}", loginRequest.getMemberId());
            return null;
        }
        Member member = optionalMember.get();
        if (!passwordEncoder.matches(loginRequest.getMemberPw(), member.getMemberPw())) {
            log.warn("로그인 실패: 비밀번호 불일치 memberId={}", loginRequest.getMemberId());
            return null;
        }
        String token = jwtUtil.generateToken(member.getMemberId(), member.getMemberPk());
        log.info("로그인 성공: memberId={}, token={}", member.getMemberId(), token);
        return token;
    }

}
