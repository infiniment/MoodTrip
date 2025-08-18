package com.moodTrip.spring.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 현재 프로젝트 루트 기준 uploads 폴더를 정적 리소스로 매핑
        String uploadPath = Paths.get(System.getProperty("user.dir"), "uploads").toUri().toString();

        if (!uploadPath.endsWith("/")) uploadPath += "/";

        registry.addResourceHandler("/image/**")
                        .addResourceLocations("classpath:/static/image/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);  // 반드시 마지막에 '/' 들어가야 함
    }
}
