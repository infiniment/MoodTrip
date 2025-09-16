package com.moodTrip.spring.config;


import com.moodTrip.spring.domain.member.entity.Member;
import com.moodTrip.spring.global.security.jwt.MyUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockMyUserDetailsSecurityContextFactory implements WithSecurityContextFactory<WithMockMyUserDetails> {
    @Override
    public SecurityContext createSecurityContext(WithMockMyUserDetails customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Mock Member 엔티티 생성
        Member mockMember = Member.builder()
                .memberPk(customUser.memberPk())
                .nickname(customUser.nickname())
                .build();

        // MyUserDetails 객체를 principal로 사용
        MyUserDetails principal = new MyUserDetails(mockMember);

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());

        context.setAuthentication(token);
        return context;
    }
}