package com.moodTrip.spring.global.security.oauth;

import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.global.security.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService; // 서비스 주입
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId(); // "kakao" or "google"
        Map<String, Object> attributes = oauth2User.getAttributes();

        String providerId = null;
        String email = null;
        String nickname = null;

        if ("kakao".equals(provider)) {
            providerId = String.valueOf(attributes.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            Map<String, Object> profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
            nickname = profile != null ? (String) profile.get("nickname") : "";
        } else if ("google".equals(provider)) {
            providerId = (String) attributes.get("sub"); // 구글의 고유 식별자
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name"); // 구글은 "name"이 전체 이름, "given_name", "family_name" 등도 있음
        } else {
            // 기타 프로바이더가 있다면 확장
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
        }

        // (아래는 기존 카카오 로직과 동일하게 재사용)
        boolean exists = memberService.existsByProviderAndProviderId(provider, providerId);
        String flowType = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("flowType".equals(cookie.getName())) {
                    flowType = cookie.getValue();
                    break;
                }
            }
        }

        if ("signup".equals(flowType)) {
            if (exists) {
                response.sendRedirect("/signup?error=이미+회원가입+된+계정입니다");
                return;
            } else {
                String memberId = provider + "_" + providerId;
                String memberPw = ""; // 소셜은 비번X
                String memberName = (nickname == null || nickname.isEmpty()) ? memberId : nickname;
                String memberPhone = "010-0000-0000";

                Member member = Member.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .memberId(memberId)
                        .memberPw(memberPw)
                        .email(email)
                        .nickname(memberName)
                        .memberPhone(memberPhone)
                        .memberAuth("U")
                        .isWithdraw(false)
                        .build();
                memberService.save(member);

                String token = jwtUtil.generateToken(member.getMemberId(), member.getMemberPk());
                Cookie jwtCookie = new Cookie("jwtToken", token);

                jwtCookie.setHttpOnly(true);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(24 * 60 * 60);
                response.addCookie(jwtCookie);

                response.sendRedirect("/signup/success");
            }
        } else {
            if (exists) {
                Member member = memberService.findByProviderAndProviderId(provider, providerId);

                String token = jwtUtil.generateToken(member.getMemberId(), member.getMemberPk());
                Cookie jwtCookie = new Cookie("jwtToken", token);
                log.info(" 소셜 JWT 토큰 발급 : {}", token);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setPath("/");
                jwtCookie.setHttpOnly(true);
                jwtCookie.setMaxAge(24 * 60 * 60);
                response.addCookie(jwtCookie);
                response.sendRedirect("/mainpage/mainpage");

            } else {
                response.sendRedirect("/signup?error=등록되지+않은+계정입니다.+회원가입이+필요합니다");
            }
        }
    }


}
