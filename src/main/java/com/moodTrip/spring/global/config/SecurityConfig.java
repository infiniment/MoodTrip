package com.moodTrip.spring.global.config;

import com.moodTrip.spring.domain.member.service.MemberService;
import com.moodTrip.spring.global.security.jwt.JwtAuthenticationEntryPoint;
import com.moodTrip.spring.global.security.jwt.JwtAuthenticationFilter;
import com.moodTrip.spring.global.security.jwt.JwtUtil;
import com.moodTrip.spring.global.security.oauth.CustomOAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    //  SuccessHandler를 Bean으로 꼭 등록! MemberService 주입 필요
    @Bean
    public AuthenticationSuccessHandler customOAuth2SuccessHandler(MemberService memberService, JwtUtil jwtUtil) {
        return new CustomOAuth2SuccessHandler(memberService, jwtUtil);
    }

    // Security 필터 체인: SuccessHandler 붙이기 (Bean 인자로 주입)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           ClientRegistrationRepository clientRegistrationRepository,
                                           MemberService memberService,
                                           JwtUtil jwtUtil,
                                           UserDetailsService userDetailsService,
                                           JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint  // ✅ 추가
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/api/login", "/signup", "/api/signup",
                                "/css/**", "/js/**", "/image/**",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**",
                                "/error", "/api/v1/room-online/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // ✅ 설정
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

        //디폴트 리졸버
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

            //커스터 마이즈 함수
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
