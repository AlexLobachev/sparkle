package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.Status;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@Service
public class HomePage {

    private final UserService userService;


    @Autowired
    public HomePage(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/")
    public String home() {
        return "entrance";
    }


    @GetMapping("/main")
    public String main(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            Model model
    )throws InterruptedException {

        User user = userService.getUserByUserName(userDetails.getUsername()).orElseThrow(() -> new NotFound("Пользователь не найден"));

        if (user.getStatus() == Status.DRAFT) {
            return "registration";  // обратно к заполнению профиля
        }

        return settings(userDetails, request, model, "main");
    }


    @GetMapping("/settings/profile")
    public String profileSettingsDuringRegistration(@AuthenticationPrincipal UserDetails userDetails,
                                                    HttpServletRequest request,
                                                    Model model
    ) {
        return settings(userDetails, request, model, "registration");
    }

    @GetMapping("/main/settings/profile")
    public String profileSettingsAfterRegistration(@AuthenticationPrincipal UserDetails userDetails,
                                                   HttpServletRequest request,
                                                   Model model
    ) {

        return settings(userDetails, request, model, "settings");
    }

    @GetMapping("/main/profile")
    public String profileUser(@AuthenticationPrincipal UserDetails userDetails,
                              HttpServletRequest request,
                              Model model) {
        return settings(userDetails, request, model, "profile");
    }

    private String settings(UserDetails userDetails,
                            HttpServletRequest request,
                            Model model,
                            String page) {
        if (userDetails instanceof User) {
            // Принудительно получаем CSRF-токен (если его нет — создаём)
            CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");


            if (csrfToken != null) {
                model.addAttribute("_csrf", csrfToken);
                // Явно передаём токен в шаблон
                model.addAttribute("csrfToken", csrfToken.getToken());
            }

            if (csrfToken != null) {
                model.addAttribute("_csrf", csrfToken);
            } else {
                return "redirect:/";
            }

            model.addAttribute("user", userDetails);

            Long userId = ((User) userDetails).getId();
            if (userId == null) {
                throw new NotFound("ID пользователя не найден");
            }
            model.addAttribute("userId", userId);
        } else {
            throw new NotFound("Пользователь не авторизован");
        }
        return page;
    }


    @GetMapping("/refresh-csrf")
    public String refreshCsrf(HttpServletRequest request) {
        // Получаем CSRF-токен из атрибутов запроса
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");

        // Если токена нет — Spring Security сгенерирует его при следующем запросе,
        // но для текущего ответа возвращаем пустую строку или дефолтное значение
        if (csrfToken == null) {
            return ""; // или можно вернуть дефолтный токен для клиента
        }

        return csrfToken.getToken();
    }


}
