package com.example.sparkle.sparkle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Конфигурация WebSocket и STOMP-брокера для чатов.
 * Настраивает:
 * - Точки подключения (STOMP over SockJS)
 * - Префиксы брокера сообщений
 * - Префиксы прикладных сообщений
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic");  // Поддержка широковещательных и персональных сообщений
        config.setApplicationDestinationPrefixes("/app"); // Префикс для входящих сообщений от клиента
        config.setUserDestinationPrefix("/user");        // Префикс для персональных сообщений
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Разрешены все домены (только для разработки!)
                .withSockJS(); // Поддержка SockJS для совместимости
    }
}