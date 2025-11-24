package com.example.sparkle.sparkle.config;

import com.example.sparkle.sparkle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.session.SimpleRedirectSessionInformationExpiredStrategy;

import java.time.Duration;

/**
 * Конфигурация безопасности приложения Sparkle.
 *
 * Настройки:
 * - Аутентификация через OAuth2 (например, Google, GitHub)
 * - Защита от CSRF с использованием куки
 * - Ограничение одной сессии на пользователя
 * - Безопасные заголовки: HSTS, CSP, frameOptions
 * - Управление сессией и куками
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserService userService;

    @Autowired
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    /**
     * Основная цепочка фильтров безопасности.
     * Определяет, как обрабатываются HTTP-запросы.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureHttpRequests(http);
        configureOAuth2Login(http);
        configureLogout(http);
        configureHeaders(http);
        configureCsrf(http);
        configureSessionManagement(http);
        return http.build();
    }

    /**
     * Настройка свойств куки сессии:
     * - Время жизни: 30 минут
     * - HttpOnly: запрет доступа из JavaScript (защита от XSS)
     * - Secure: передача только по HTTPS
     * - SameSite: Lax — помогает защититься от атак CSRF
     */
    @Bean
    public Cookie sessionCookie() {
        Cookie cookie = new Cookie();
        cookie.setMaxAge(Duration.ofMinutes(30)); // 30 минут (более читаемо, чем 1800 секунд)
        cookie.setHttpOnly(true);  // Защита от XSS
        cookie.setSecure(true);    // Только по HTTPS
        cookie.setSameSite(Cookie.SameSite.LAX); // Баланс между безопасностью и удобством
        return cookie;
    }

    /**
     * Настройка доступа к URL:
     * - Разрешены: главная, логин, OAuth2, статические ресурсы (CSS, JS)
     * - Все остальные: требуют аутентификации
     */
    private void configureHttpRequests(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login/**", "/oauth2/**", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
        );
    }

    /**
     * Настройка входа через OAuth2:
     * - Использует кастомный обработчик успеха, чтобы при первом входе создать пользователя в БД
     */
    private void configureOAuth2Login(HttpSecurity http) throws Exception {
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2LoginSuccessHandler()) // Обработчик после успешного входа
        );
    }

    /**
     * Настройка выхода из системы:
     * - URL: /logout
     * - После выхода перенаправляет на главную
     * - Удаляет куки сессии
     * - Завершает сессию
     * - Очищает аутентификацию
     * - Дополнительный обработчик для логирования
     */
    private void configureLogout(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID", "JSESSIONIDSSO", "SESSION")
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .addLogoutHandler(new SecurityContextLogoutHandler())
                .addLogoutHandler((request, response, authentication) -> {
                    // Лучше использовать logger, а не System.out
                    System.out.println("Сессия уничтожена: " + request.getSession(false));
                })
        );
    }

    /**
     * Настройка HTTP-заголовков для безопасности:
     * - frameOptions: sameOrigin — защита от clickjacking
     * - HSTS: принудительное использование HTTPS в течение года
     * - CSP: политика безопасности контента (ограничение источников)
     */
    private void configureHeaders(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000) // 1 год
                        .preload(true)
                )
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:; " +
                                        "font-src 'self'; " +
                                        "object-src 'none';"
                        )
                )
        );
    }

    /**
     * Настройка защиты от CSRF:
     * - Токен хранится в куке (CookieCsrfTokenRepository)
     * - Кука доступна для чтения JavaScript (для SPA), но установлена как SameSite=Lax
     * - Игнорируется для /logout (обычно вызывается без токена)
     * - Для REST API можно добавить "/api/**" в игнор
     *
     * Также включает стандартную форму логина (если нужно)
     */
    private void configureCsrf(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .ignoringRequestMatchers("/logout", "/api/**")
        );
        http.formLogin(Customizer.withDefaults());
    }

    /**
     * Хранилище CSRF-токена в куке.
     * withHttpOnlyFalse() — позволяет JavaScript прочитать токен (например, для отправки в заголовке).
     */
    private CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/"); // Доступно по всему приложению
        return repository;
    }

    /**
     * Управление сессиями:
     * - Создание сессии при необходимости
     * - Только одна активная сессия на пользователя
     * - При превышении лимита — старая сессия завершается
     * - Перенаправление на /login?expired=true при истечении сессии
     */
    private void configureSessionManagement(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false) // Новая сессия завершает старую
                .expiredSessionStrategy(
                        new SimpleRedirectSessionInformationExpiredStrategy("/login?expired=true")
                )
        );
    }

    /**
     * Бин: обработчик успешного входа через OAuth2.
     * Вызывается после аутентификации, чтобы сохранить/обновить пользователя.
     */
    @Bean
    public AuthenticationSuccessHandler oauth2LoginSuccessHandler() {
        return new OAuth2LoginSuccessHandler(userService);
    }
}