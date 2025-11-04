package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.model.ChatMessage;
import com.example.sparkle.sparkle.service.ChatService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sparkle/chat")
public class ChatController {

    private final ChatService chatService;


    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;

    }

    /**
     * Отображение списка чатов текущего пользователя
     */
    @GetMapping("/chats/users/{userId}")
    public ResponseEntity<?> listChatsForCurrentUser(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.listChatsForCurrentUser(userId));
    }

    /**
     * Создание нового чата
     */
    @PostMapping()
    public ResponseEntity<?> createChat(@RequestParam @Min(1) Long senderId,
                                        @RequestParam @Min(1) Long receiverId) {
        return ResponseEntity.created(null).body(chatService.createChat(senderId, receiverId));
    }


    /**
     * Отправка сообщения другому пользователю
     */
    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestParam @Min(1) Long chatId,
                                         @RequestParam @Min(1) Long senderId,
                                         @RequestBody ChatMessage chatMessage) {
        return ResponseEntity.created(null).body(chatService.sendMessage(senderId, chatId, chatMessage));
    }

    /**
     * История сообщений для определенного чата
     */
    @GetMapping("/history/{chatId}/users/{userId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long userId, @PathVariable Long chatId) {
        return ResponseEntity.ok().body(chatService.getChatHistory(userId, chatId)
                .stream()
                .map(ChatMessage::toMessageDtoHistory));
    }

    /**
     * Удаление чата для одного пользователя
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteChat(@RequestParam Long userId, @RequestParam Long chatId) {

        return ResponseEntity.ok().body(chatService.deleteChat(userId, chatId));
    }
}
