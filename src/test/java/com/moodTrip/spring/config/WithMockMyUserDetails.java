package com.moodTrip.spring.config;


import org.springframework.security.test.context.support.WithSecurityContext;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockMyUserDetailsSecurityContextFactory.class)
public @interface WithMockMyUserDetails {
    long memberPk() default 1L; // 기본 사용자 PK
    String nickname() default "testUser"; // 기본 사용자 닉네임
    // 필요시 다른 사용자 정보도 추가 가능
}