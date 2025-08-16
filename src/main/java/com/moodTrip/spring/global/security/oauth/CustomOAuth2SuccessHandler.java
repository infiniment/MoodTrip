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
            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다.");
        }

        boolean exists = memberService.existsByProviderAndProviderId(provider, providerId);

        // 상우가 추가 여기서 flowType 체크 (명시적으로 로그인 요청한 경우만 JWT 발급)
        String flowType = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("flowType".equals(cookie.getName())) {
                    flowType = cookie.getValue();
                    break;
                }
            }
        }

        // 상우가 추가flowType 없으면 (즉, 브라우저 세션으로 자동 로그인된 경우) 메인 페이지로만 이동, JWT는 발급하지 않음
        if (flowType == null) {
            log.info("🔹 OAuth2 자동 로그인 감지 - JWT 발급 없이 메인 페이지 이동");
            response.sendRedirect("/");
            return;
        }

        if ("signup".equals(flowType)) {
            if (exists) {
                response.sendRedirect("/signup?error=이미+회원가입+된+계정입니다");
                return;
            } else {
                String memberId = provider + "_" + providerId;
                String memberPw = "";
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
        } else if ("login".equals(flowType)) {
            if (exists) {
                Member member = memberService.findByProviderAndProviderId(provider, providerId);

                // 상우가 추가 탈퇴 회원인지 체크
                if (Boolean.TRUE.equals(member.getIsWithdraw())) {
                    log.warn("❌ 탈퇴한 회원 로그인 시도 - memberId: {}", member.getMemberId());
                    response.sendRedirect("/withdraw");
                    return; // JWT 발급 안 하고 종료
                }

                // JWT 발급
                String token = jwtUtil.generateToken(member.getMemberId(), member.getMemberPk());
                Cookie jwtCookie = new Cookie("jwtToken", token);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(24 * 60 * 60);
                response.addCookie(jwtCookie);
                response.sendRedirect("/mainpage/mainpage");
            } else {
                response.sendRedirect("/signup?error=등록되지+않은+계정입니다.+회원가입이+필요합니다");
            }
        }
    }
}