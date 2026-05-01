package com.petify.petify.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Path.of("uploads").toAbsolutePath().normalize();
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations(uploadPath.toUri().toString() + "/");
    }
}
