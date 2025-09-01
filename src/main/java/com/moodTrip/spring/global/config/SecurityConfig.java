package com.moodTrip.spring.global.config;

import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.global.security.jwt.JwtAuthenticationEntryPoint;
import com.moodTrip.spring.global.security.jwt.JwtAuthenticationFilter;
import com.moodTrip.spring.global.security.jwt.JwtUtil;
import com.moodTrip.spring.global.security.oauth.CustomOAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/uploads/**")
                .requestMatchers("/css/**", "/js/**", "/image/**");
    }

    @Bean
    public AuthenticationSuccessHandler customOAuth2SuccessHandler(MemberService memberService, JwtUtil jwtUtil) {
        return new CustomOAuth2SuccessHandler(memberService, jwtUtil);
    }

    /**
     * 커스텀 AuthenticationEntryPoint - HTML과 JSON 요청을 구분해서 처리
     */
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response,
                                 AuthenticationException authException) throws IOException {

                String requestURI = request.getRequestURI();
                String acceptHeader = request.getHeader("Accept");

                System.out.println("인증되지 않은 접근 - URI: " + requestURI + ", Accept: " + acceptHeader);

                // API 요청인 경우 JSON 응답
                if (requestURI.startsWith("/api/") ||
                        (acceptHeader != null && acceptHeader.contains("application/json"))) {

                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"로그인이 필요합니다.\",\"redirect\":\"/login\"}");

                } else {
                    // HTML 페이지 요청인 경우 로그인 페이지로 리다이렉트
                    System.out.println("로그인 페이지로 리다이렉트: " + requestURI);
                    response.sendRedirect("/login?returnUrl=" + java.net.URLEncoder.encode(requestURI, "UTF-8"));
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           ClientRegistrationRepository clientRegistrationRepository,
                                           MemberService memberService,
                                           JwtUtil jwtUtil,
                                           UserDetailsService userDetailsService
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/logout", "/login", "/api/login", "/signup",
                                "/css/**", "/js/**", "/image/**", "/uploads/**",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**",
                                "/error", "/api/v1/room-online/**", "/api/v1/profiles/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/attractions/content/*/emotion-tags").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/attractions/content/*/detail").permitAll()

                        // 마이페이지 인증 필요 설정
                        .requestMatchers("/mypage/**").authenticated()

                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint()) // 커스텀 EntryPoint 사용
                )
                .formLogin(form -> form.disable())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestResolver(
                                        customAuthorizationRequestResolver(clientRegistrationRepository)
                                )
                        )
                        .successHandler(customOAuth2SuccessHandler(memberService, jwtUtil))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies("jwtToken")
                        .invalidateHttpSession(true)
                );

        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil, userDetailsService),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
                return customize(authorizationRequest);
            }

            @Override
            public org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
                return customize(authorizationRequest);
            }

            private org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest customize(
                    org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest original) {
                if (original == null) return null;
                Map<String, Object> extra = new HashMap<>(original.getAdditionalParameters());
                extra.put("prompt", "login");
                return org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest.from(original)
                        .additionalParameters(extra)
                        .build();
            }
        };
    }
}