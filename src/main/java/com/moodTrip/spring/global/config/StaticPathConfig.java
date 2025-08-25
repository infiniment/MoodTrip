package com.moodTrip.spring.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticPathConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /static/** URL => classpath:/static/** 파일 매핑
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}