package com.example.sparkle.sparkle.config;

import com.example.sparkle.sparkle.model.*;
import com.example.sparkle.sparkle.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Обработчик успешного OAuth2 входа: аутентификация или регистрация пользователя.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    private static final String MAIN_PAGE = "/main";
    private static final String SETTINGS_PAGE = "/settings/profile";
    private static final String LOGIN_ERROR_PAGE = "/entrance?error";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauthUser = oauth2Token.getPrincipal();
            String provider = oauth2Token.getAuthorizedClientRegistrationId();



            String externalId = Objects.requireNonNull(oauthUser.getAttribute("id")).toString();
            String username = resolveUsername(oauthUser, provider);
            String email = oauthUser.getAttribute("email");

            User user = userService.findByExternalIdAndProvider(externalId, provider)
                    .orElseGet(() -> registerNewUser(externalId, username, email, provider));

            authenticateUser(user, request, response);
            if (user.getStatus() == Status.DRAFT) {
                response.sendRedirect(SETTINGS_PAGE);
            } else {
                response.sendRedirect(MAIN_PAGE);
            }
        } catch (Exception e) {
            log.error("OAuth2 аутентификация не удалась", e);
            response.sendRedirect(LOGIN_ERROR_PAGE);
        }
    }

    private void authenticateUser(User user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);


    }

    @Transactional
    User registerNewUser(String externalId, String username, String email, String provider) {
        log.info("Регистрация нового пользователя: externalId={}, provider={}", externalId, provider);

        User user = new User();
        user.setExternalId(externalId);
        user.setUsername(username);
        if (email != null && isValidEmail(email)) {
            user.setEmail(email);
        }
        user.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
        user.setStatus(Status.DRAFT); // 🛑 Всегда DRAFT при регистрации

        UserRoles role = new UserRoles();
        role.setRoles(Roles.USER);
        role.setUser(user);

        user.setRoles(Set.of(role));

        return userService.registerUserBySocialNetwork(user).orElseThrow();
    }

    private String resolveUsername(OAuth2User oauthUser, String provider) {
        return switch (provider.toLowerCase()) {
            case "github" -> {
                String login = oauthUser.getAttribute("login");
                String id = String.valueOf(oauthUser.getAttribute("id").toString());
                yield login != null ? login : "github_user_" + id;
            }
            case "vkontakte", "vk" -> {
                String firstName = oauthUser.getAttribute("first_name");
                String lastName = oauthUser.getAttribute("last_name");
                String name = Stream.of(firstName, lastName)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining(" "));
                yield name.isEmpty() ? "vk_user_" + oauthUser.getAttribute("id") : name;
            }
            case "google" -> {
                String name = oauthUser.getAttribute("name");
                yield name != null ? name : "google_user_" + oauthUser.getAttribute("sub");
            }
            default -> "user_" + oauthUser.getAttribute("id");
        };
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}