package com.example.sparkle.sparkle.config;

import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.AuthProvider;
import com.example.sparkle.sparkle.model.Roles;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserRoles;
import com.example.sparkle.sparkle.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    // Допустимые провайдеры аутентификации
    private static final Set<String> VALID_PROVIDERS = Set.of("github", "google", "vk", "vkontakte");
    // Константы для перенаправлений
    private static final String MAIN_PAGE = "/main";
    private static final String SETTINGS_PAGE = "/settings/profile";
    private static final String ERROR_PAGE = "/error";
    private static final String LOGIN_ERROR_PAGE = "/login?error=user_not_found";


    @Autowired
    public OAuth2LoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. Проверяем, что аутентификация имеет ожидаемый тип
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            log.warn("Invalid authentication type: {}", authentication.getClass());
            response.sendRedirect(ERROR_PAGE);
            return;
        }

        OAuth2User oauthUser = oauthToken.getPrincipal();
        String externalId = oauthUser.getAttribute("id").toString();
        String provider = oauthToken.getAuthorizedClientRegistrationId();


        // Логируем попытку входа
        log.info("OAuth2 login attempt: provider={}, externalId={}", provider, externalId);

        try {
            // 2. Ищем пользователя в БД по внешнему ID и провайдеру
            User existingUser = userService.findByExternalIdAndProvider(externalId, provider)
                    .orElse(null);

            if (existingUser != null) {
                // 3. Если пользователь найден — авторизуем его в системе

                authenticateUser(existingUser, request, response);
                return;
            }

            // 4. Если пользователь не найден — регистрируем нового
            registerNewUser(oauthToken, response, request);

        } catch (NotFound e) {
            // Пользователь не найден в БД (хотя должен был быть)
            log.warn("User not found: {}", externalId);
            response.sendRedirect(LOGIN_ERROR_PAGE);
        } catch (IllegalArgumentException e) {
            // Некорректный провайдер или данные
            log.error("Invalid provider or data: {}", provider, e);
            response.sendRedirect("/error?invalid_provider");
        } catch (Exception e) {
            // Неожиданная ошибка
            log.error("OAuth2 login failed", e);
            response.sendRedirect(ERROR_PAGE);
        }
    }

    /**
     * Авторизует существующего пользователя в системе:
     * - создаёт Authentication-объект;
     * - помещает его в SecurityContext;
     * - сохраняет контекст в HttpSession;
     * - перенаправляет на главную страницу.
     */
    private void authenticateUser(User user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user,                     // principal (пользователь)
                null,                    // credentials (не нужны для уже авторизованного)
                user.getAuthorities()     // роли/права пользователя
        );


        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authToken);


        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        log.info("User authenticated successfully: id={}, username={}", user.getId(), user.getUsername());

        response.sendRedirect(MAIN_PAGE);
    }

    /**
     * Регистрирует нового пользователя на основе данных OAuth2.
     * Заполняет поля: provider, externalId, username, email (если есть).
     * Сохраняет пользователя в БД и перенаправляет на главную страницу.
     */
    @Transactional
    void registerNewUser(OAuth2AuthenticationToken oauthToken, HttpServletResponse response, HttpServletRequest request) throws IOException {
        OAuth2User oauthUser = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        if (!VALID_PROVIDERS.contains(provider)) {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        User user = new User();
        user.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
        user.setExternalId(oauthUser.getAttribute("id").toString());

        String username = resolveUsername(oauthUser, provider);
        user.setUsername(username);

        String email = oauthUser.getAttribute("email");
        if (email != null && isValidEmail(email)) {
            user.setEmail(email);
        }

        UserRoles userRoles = new UserRoles();
        userRoles.setRoles(Roles.USER);
        userRoles.setUser(user);
        user.setRoles(Set.of(userRoles));

        // Сохраняем пользователя
        userService.registerUserBySocialNetwork(user);
        log.info("New user registered: id={}, provider={}, username={}", user.getId(), provider, username);

        // Обновляем SecurityContext
        updateSecurityContext(user, oauthToken, request);


        response.sendRedirect(SETTINGS_PAGE);
    }

    private void updateSecurityContext(User user, OAuth2AuthenticationToken originalToken, HttpServletRequest request) {
        // 1. Получаем атрибуты пользователя из оригинального токена
        Map<String, Object> attributes = originalToken.getPrincipal().getAttributes().entrySet().stream()
                .collect(Collectors.toMap(
                        (Map.Entry<String, Object> entry) -> entry.getKey(),
                        entry -> entry.getValue()
                ));

        // 2. Добавляем email, если есть
        if (user.getEmail() != null) {
            attributes.put("email", user.getEmail());
        }

        // 3. Формируем полномочия (роли) пользователя
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Если getAuthorities() может вернуть null, добавьте проверку:
        // if (authorities == null) authorities = Collections.emptySet();

        // 4. Создаём нового OAuth2User с корректными аргументами
        DefaultOAuth2User updatedUser = new DefaultOAuth2User(
                authorities,           // 1-й аргумент: полномочия
                attributes,            // 2-й аргумент: атрибуты
                "sub"                 // 3-й аргумент: ключ имени (обычно "sub")
        );

        // 5. Создаём новый токен аутентификации
        OAuth2AuthenticationToken newToken = new OAuth2AuthenticationToken(
                updatedUser,
                authorities,
                originalToken.getAuthorizedClientRegistrationId()
        );

        // 6. Обновляем SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(newToken);
        SecurityContextHolder.setContext(context);

        // 7. Сохраняем в HttpSession
        HttpSession session = request.getSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }


    /**
     * Формирует username на основе данных провайдера.
     * Для VK объединяет first_name и last_name, для остальных — берёт готовое поле.
     */
    private String resolveUsername(OAuth2User oauthUser, String provider) {
        switch (provider) {
            case "github" -> {
                return oauthUser.getAttribute("login");
            }
            case "google" -> {
                return oauthUser.getAttribute("name");
            }
            case "vk", "vkontakte" -> {
                String firstName = oauthUser.getAttribute("first_name");
                String lastName = oauthUser.getAttribute("last_name");

                return Optional.of(
                                Stream.of(firstName, lastName)
                                        .filter(Objects::nonNull)
                                        .map(String::trim)
                                        .filter(s -> !s.isEmpty())
                                        .collect(Collectors.joining(" "))
                        )
                        .orElse("VKUser_" + oauthUser.getAttribute("id"));

            }
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    /**
     * Проверяет корректность email-адреса с помощью регулярного выражения.
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}


