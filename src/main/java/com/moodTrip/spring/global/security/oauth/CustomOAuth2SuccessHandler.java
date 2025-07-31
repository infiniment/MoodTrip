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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId(); // "kakao"

        Map<String, Object> attributes = oauth2User.getAttributes();
        String providerId = String.valueOf(attributes.get("id"));

        // 1. 이미 가입된 소셜 회원인지 체크
        boolean exists = memberService.existsByProviderAndProviderId(provider, providerId);
        // 인증 성공 후, 쿠키에서 flowType 읽기
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
                // 이미 가입된 회원인 경우 안내 + 회원가입 폼으로 리다이렉트 (에러 메시지 포함)
                response.sendRedirect("/signup?error=이미+회원가입+된+계정입니다");
                return;
            }else{
                // 2. 신규 회원만 저장
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
                Map<String, Object> profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
                String nickname = profile != null ? (String) profile.get("nickname") : "";

                String memberId = "kakao_" + providerId;
                String memberPw = ""; // 소셜 로그인은 비번 사용 X
                String memberName = nickname == null ? memberId : nickname;
                String memberPhone = "010-0000-0000";

                Member member = Member.builder()
                        .provider(provider)
                        .providerId(providerId)
                        .memberId(memberId)
                        .memberPw(memberPw)
                        .email(email)
                        .memberName(memberName)
                        .memberPhone(memberPhone)
                        .memberAuth("U")
                        .isWithdraw(false)
                        .build();
                memberService.save(member);

                // 4. 저장 후 JWT 토큰 발급 및 쿠키 저장
                String token = jwtUtil.generateToken(member.getMemberId(), member.getMemberPk());
                Cookie jwtCookie = new Cookie("jwtToken", token);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(24 * 60 * 60); // 1일
                response.addCookie(jwtCookie);



                response.sendRedirect("/signup/success");
            }

        }else{
            if (exists) {
                // 회원이면 로그인 허용
                Member member = memberService.findByProviderAndProviderId(provider, providerId);

                // 2. JWT 토큰 발급 및 쿠키 저장
                String token = jwtUtil.generateToken(member.getMemberId(), member.getMemberPk());
                log.info("카카오 소셜 JWT 토큰 발급 : {}", token);  // 로그에서 값 직접 확인
                Cookie jwtCookie = new Cookie("jwtToken", token);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(24 * 60 * 60); // 1일
                response.addCookie(jwtCookie);

                response.sendRedirect("/mainpage/mainpage");
            } else {
                // 회원정보 없음
                response.sendRedirect("/signup?error=등록되지+않은+계정입니다.+회원가입이+필요합니다");
            }
        }





    }

}
