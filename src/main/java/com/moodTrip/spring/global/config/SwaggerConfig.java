package com.moodTrip.spring.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI moodTripAPI() {
        return new OpenAPI()
                .info(moodTripInfo())
                .addServersItem(new Server().url("/"))
                // JWT Security 설정 추가
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        ));
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
}
