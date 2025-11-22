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
import org.springframework.security.web.session.SimpleRedirectSessionInformationExpiredStrategy;

import java.time.Duration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private UserService userService;

    @Bean
    public AuthenticationSuccessHandler oauth2LoginSuccessHandler(UserService userService) {
        return new OAuth2LoginSuccessHandler(userService);
    }

    @Autowired
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

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

    @Bean
    public Cookie sessionCookie() {
        Cookie cookie = new Cookie();
        cookie.setMaxAge(Duration.ofSeconds(1800)); // 30 минут в секундах
        return cookie;
    }

    private void configureHttpRequests(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login/**", "/oauth2/**", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
        );
    }

    private void configureOAuth2Login(HttpSecurity http) throws Exception {
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2LoginSuccessHandler(userService))
        );
    }

    private void configureLogout(HttpSecurity http) throws Exception {
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID", "JSESSIONIDSSO", "SESSION") // Очищаем куки сессии
                .clearAuthentication(true)// Очищаем аутентификацию
                .invalidateHttpSession(true)// Завершаем сессию
                .addLogoutHandler(new SecurityContextLogoutHandler())
                .addLogoutHandler((request, response, authentication) -> {
                    System.out.println("Сессия уничтожена: " + request.getSession(false));
                })
        );
    }

    private void configureHeaders(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        ).contentSecurityPolicy((HeadersConfigurer<HttpSecurity>.ContentSecurityPolicyConfig contentSecurityPolicyConfig) -> {
                                    contentSecurityPolicyConfig.policyDirectives("default-src 'self' https://*");

                                }
                        ));
    }

    private void configureCsrf(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())  // Сохраняем токен в куки
                        .ignoringRequestMatchers("/logout")
                )
                .formLogin(Customizer.withDefaults()


                );


    }

    private void configureSessionManagement(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .expiredSessionStrategy(
                        new SimpleRedirectSessionInformationExpiredStrategy("/login?expired=true")

                )

        );
    }
}
