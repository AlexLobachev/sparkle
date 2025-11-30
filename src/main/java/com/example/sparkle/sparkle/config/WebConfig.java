package com.example.sparkle.sparkle.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // Основная директория с фото
//        String uploadDir = "C:/Users/Mi/Documents/rep2025/UserPhoto/";
//
//        registry.addResourceHandler("/images/**")
//                .addResourceLocations("file:" + uploadDir)
//                .setCachePeriod(0);
//
//        // Дополнительная проверка: логируем путь (для отладки)
//        System.out.println("📁 Фотографии доступны по: file:" + uploadDir);
//        System.out.println("🌐 URL для браузера: http://localhost:8080/images/testuserMary.jpeg");
//        log.debug("🌐 URL для браузера: http://localhost:8080/images/testuserMary.jpeg");
//    }
//}

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Обработчик для пользовательских фото (ваш текущий код)
        String uploadDir = "C:/Users/Mi/Documents/rep2025/UserPhoto/";
        registry.addResourceHandler("/images/**") // Новый путь!
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(0);

        // 2. Обработчик для статических иконок (из classpath)
        registry.addResourceHandler("/images/icons/**")
                .addResourceLocations("classpath:/static/images/icons/")
                .setCachePeriod(0);

        // Логи
        System.out.println("📁 Фотографии пользователей: file:" + uploadDir);
        System.out.println("🌐 URL фото: http://localhost:8080/images/user-photos/testuserMary.jpeg");
        System.out.println("🌐 URL иконок: http://localhost:8080/images/icons/dislike.svg");
    }

}