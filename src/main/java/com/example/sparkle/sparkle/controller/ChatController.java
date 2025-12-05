package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.chat.MessageDtoHistory;
import com.example.sparkle.sparkle.model.ChatMessage;
import com.example.sparkle.sparkle.service.ChatService;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sparkle/chats")
@Slf4j
public class ChatController {

    private final ChatService chatService;


    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;

    }

    /**
     * Отображение списка чатов текущего пользователя
     */
    @GetMapping("/users")
    public ResponseEntity<?> listChatsForCurrentUser() {
        return ResponseEntity.ok(chatService.listChatsForCurrentUser());
    }

    /**
     * Создание нового чата
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{receiverId}")
    public ResponseEntity<?> createChat(@PathVariable @Min(1) Long receiverId
                                        ) {
        System.out.println("🔹 Запрос на создание чата с " + receiverId);
        return ResponseEntity.created(null).body(chatService.createChat(receiverId));
    }


        /**
     * История сообщений для определенного чата
     */
    @GetMapping("{chatId}/history")
    public ResponseEntity<?> getChatHistory(@PathVariable Long chatId) {
        return ResponseEntity.ok().body(chatService.getChatHistory(chatId));
    }

    /**
     * Удаление чата для одного пользователя
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteChat(@RequestParam Long chatId) {
        chatService.deleteChat(chatId);
        return ResponseEntity.ok().build();
    }

    /**
     * Удаление сообщения, сообщение удаляется у всех пользователей
     */
    @DeleteMapping("/message/{messageId}")
    public void deleteMessage(@PathVariable Long messageId) {
        chatService.deleteMessage(messageId);
    }
}
