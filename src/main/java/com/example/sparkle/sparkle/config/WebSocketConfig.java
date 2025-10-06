package com.example.sparkle.sparkle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Настройка брокера сообщений
        config.enableSimpleBroker("/topic"); // Канал для широковещательных сообщений
        config.setApplicationDestinationPrefixes("/app"); // Префикс для маршрутов приложения
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Регистрация конечной точки WebSocket
        registry.addEndpoint("/ws").withSockJS(); // Конечная точка для подключения через WebSocket
    }
}
