package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.model.ChatMessage;
import com.example.sparkle.sparkle.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

/**
 * Контроллер для обработки WebSocket-сообщений чата.
 * Принимает сообщения через STOMP и делегирует их сервису.
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    /**
     * Обрабатывает входящие сообщения от клиентов.
     * Клиент отправляет на /app/chat.send → попадает сюда.
     *
     * @param message входящее сообщение чата
     */
    @MessageMapping("/chat.send")
    public void processMessage(@Payload ChatMessage message) {
        chatService.sendMessage(message);
    }

}
