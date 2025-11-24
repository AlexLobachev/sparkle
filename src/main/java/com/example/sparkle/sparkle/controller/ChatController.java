package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.chat.MessageDtoHistory;
import com.example.sparkle.sparkle.model.ChatMessage;
import com.example.sparkle.sparkle.service.ChatService;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @PostMapping()
    public ResponseEntity<?> createChat(@RequestParam @Min(1) Long senderId,
                                        @RequestParam @Min(1) Long receiverId) {
        return ResponseEntity.created(null).body(chatService.createChat(senderId, receiverId));
    }


    /**
     * Отправка сообщения другому пользователю
     */
    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessage savedMessage) {
        return ResponseEntity.created(null).body(chatService.sendMessage(savedMessage));
    }
        /**
     * История сообщений для определенного чата
     */
    @GetMapping("{chatId}/history")
    public ResponseEntity<?> getChatHistory(@PathVariable Long chatId) {
        return ResponseEntity.ok().body(chatService.getChatHistory(chatId)
                .stream()
                .map(MessageDtoHistory::toMessageDto));
    }

    /**
     * Удаление чата для одного пользователя
     */
    @DeleteMapping()
    public ResponseEntity<?> deleteChat(@RequestParam Long userId, @RequestParam Long chatId) {

        return ResponseEntity.ok().body(chatService.deleteChat(userId, chatId));
    }
}
