package com.example.sparkle.sparkle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Основная директория с фото
        String uploadDir = "C:/Users/Mi/Documents/rep2025/UserPhoto/";

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(0);

        // Дополнительная проверка: логируем путь (для отладки)
        System.out.println("📁 Фотографии доступны по: file:" + uploadDir);
        System.out.println("🌐 URL для браузера: http://localhost:8080/images/testuserMary.jpeg");
        log.debug("🌐 URL для браузера: http://localhost:8080/images/testuserMary.jpeg");
    }
}