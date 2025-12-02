package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.model.*;
import com.example.sparkle.sparkle.security.CustomOAuth2User;
import com.example.sparkle.sparkle.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class VKLoginController {

    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/login/vk/callback")
    public void callback(@RequestParam String accessToken,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        try {
            // 1. Получаем данные пользователя из VK API
            Map<String, Object> userData = fetchVkUserData(accessToken);
            if (userData == null || !userData.containsKey("id")) {
                response.sendRedirect("/entrance?error");
                return;
            }

            String externalId = userData.get("id").toString();
            String firstName = (String) userData.get("first_name");
            String lastName = (String) userData.get("last_name");
            String email = (String) userData.get("email");

            String username = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
            username = username.trim().isEmpty() ? "vk_user_" + externalId : username.trim();
            final String finalUsername = username;
            // 2. Ищем или создаём пользователя
            User user = userService.findByExternalIdAndProvider(externalId, "vkontakte")
                    .orElseGet(() -> registerNewVkUser(externalId, finalUsername, email));

            // 3. Аутентифицируем
            authenticateUser(user, request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("/entrance?error");
        }
    }

    private Map<String, Object> fetchVkUserData(String accessToken) {
        String url = "https://api.vk.com/method/users.get" +
                "?access_token=" + accessToken +
                "&fields=first_name,last_name,email" +
                "&v=5.131";

        try {
            ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = resp.getBody();
            if (body != null && body.containsKey("response")) {
                List<Map<String, Object>> responseList = (List<Map<String, Object>>) body.get("response");
                if (!responseList.isEmpty()) {
                    return responseList.get(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private User registerNewVkUser(String externalId, String username, String email) {
        User user = new User();
        user.setExternalId(externalId);
        user.setUsername(username);
        user.setEmail(email);
        user.setProvider(AuthProvider.VKONTAKTE);
        user.setStatus(Status.DRAFT); // ← Важно: при создании — статус DRAFT

        UserRoles role = new UserRoles();
        role.setRoles(Roles.USER);
        role.setUser(user);
        user.setRoles(Set.of(role));

        return userService.registerUserBySocialNetwork(user).orElseThrow();
    }

    private void authenticateUser(User user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user, null, authorities);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        if (user.getStatus().equals(Status.DRAFT)) {
            log.info("Профиль не заполнен, перенаправляем на /settings/profile");
            response.sendRedirect("/settings/profile");
        } else {
            log.info("Профиль заполнен, перенаправляем на /main");
            response.sendRedirect("/main");
        }
    }

    @GetMapping("/login/vk/exchange")
    public String exchangePage(HttpServletRequest request, Model model) {
        // Передаём параметры из URL в шаблон
        String code = request.getParameter("code");
        String deviceId = request.getParameter("device_id");

        model.addAttribute("code", code);
        model.addAttribute("deviceId", deviceId);
        model.addAttribute("cspNonce", SecurityUtils.generateCspNonce());

        return "vk-exchange"; // Thymeleaf шаблон
    }


}

