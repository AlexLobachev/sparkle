//package com.example.sparkle.sparkle.config;
//
//        import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
//
//        @Configuration
//        @EnableWebSecurity
//        public class SecurityConfig {
//
//                @Bean
//                public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//                return http
//                                        .authorizeHttpRequests(auth -> auth
//                                                                .anyRequest().authenticated()
//                                        )
//                                        .oauth2Login(oauth2 -> oauth2
//                                                                .loginPage("/login")
//                                                // Убираем устаревший redirectionEndpoint()
//                                                .defaultSuccessUrl("/")
//                                        )
//                        .logout(logout -> logout
//                                                                .logoutUrl("/logout")
//                                                .logoutSuccessUrl("/")
//                                        )
//                        .csrf(csrf -> csrf
//                                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                                        )
//                                        .sessionManagement(session -> session
//                                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                                        )
//                                        .build();
//
//
//            }
//    }


