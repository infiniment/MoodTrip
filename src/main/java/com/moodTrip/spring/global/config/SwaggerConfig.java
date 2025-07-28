package com.moodTrip.spring.global.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // 스프링 실행 시 설정 파일 읽어들이기 위한 애노테이션
public class SwaggerConfig {
    @Bean
    public OpenAPI moodTripAPI() {
        return new OpenAPI()
                .info(moodTripInfo())
                .addServersItem(new Server().url("/"));
        // 기본 서버 URL(로컬 및 서버 공통)
    }

    @Bean
    public GroupedOpenApi defaultApi() {
        String[] pathsToMatch = {"/api/**"};

        return GroupedOpenApi.builder()
                .group("default")
                .displayName("MoodTrip 기본 API")
                .pathsToMatch(pathsToMatch)
                .build();
    }

    private Info moodTripInfo() {
        return new Info()
                .title("MoodTrip API")
                .description("감정 기반 여행 추천 및 동행자 매칭 서비스 API 명세서")
                .version("1.0.0");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/signup", "/css/**", "/js/**", "/image/**",
                                // Swagger 관련 경로 추가
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
