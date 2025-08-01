package com.moodTrip.spring.domain.member.service;

import com.moodTrip.spring.domain.member.dto.request.MemberRequest;
import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(MemberRequest request) {

        // 유효성 검증
        // 중복 확인
        // 비번 암호화
        log.info("회원가입 요청 처리 시작 - {}", request.getUserId());

        // 엔티티 변환 및 저장
        if (!request.isTerms()) {
            throw new IllegalArgumentException("이용약관에 동의해야 회원가입이 가능합니다.");
        }
        // 비밀번호 중복
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 아이디 중복 체크
        if (memberRepository.existsByMemberId(request.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 이메일 중복 체크
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 엔티티 객체로 변환
        Member member = Member.builder()
                .memberId(request.getUserId())
                .memberPw(encodedPassword)
                .email(request.getEmail())
                .nickname(request.getUserId())// 추가
                .memberAuth("U")                        // 기본 권한 값 예시
                .nickname(request.getUserId()) // 우선 아이디로만 설정
                .memberPhone("010-0000-0000")           // 필수면 기본값 또는 폼 추가 필요
                .isWithdraw(false)
                .build();
        try {
            memberRepository.save(member);
            log.info("회원가입 성공 - {}", member.getMemberId());
        } catch (Exception e) {
            log.error("회원가입 저장 중 오류 발생", e);
            throw e;  // 예외 다시 던지기 (필요 시)
        }
    }

    //소셜로그인 서비스 로직
    public void save(Member member) {
        memberRepository.save(member);
    }

    //소셜아이디ㅣ 유효성 검사
    public boolean existsByProviderAndProviderId(String provider, String providerId) {
        return memberRepository.existsByProviderAndProviderId(provider, providerId);
    }


    // provider와 providerId로 회원찾기(소셜 로그인)
    public Member findByProviderAndProviderId(String provider, String providerId) {
        return memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다"));
    }

}

