package com.moodTrip.spring.testsupport;

import com.moodTrip.spring.domain.member.entity.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Member mockMember = Member.builder()
                .memberPk(annotation.memberPk())
                .memberId(annotation.username())
                .nickname(annotation.nickname())
                .isWithdraw(false)
                .build();

        var auth = new UsernamePasswordAuthenticationToken(
                mockMember,
                "mockPassword",
                Collections.emptyList()
        );

        context.setAuthentication(auth);

        return context;
    }
}
