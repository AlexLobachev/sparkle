package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.MessageDTO;
import com.example.sparkle.sparkle.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

private final ChatService chatService;
private final SimpMessagingTemplate messagingTemplate;

@Autowired
public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
    this.chatService = chatService;
    this.messagingTemplate = messagingTemplate;
}

    /**
     * Отображение списка чатов текущего пользователя
     */
 @GetMapping("/chats")
 public ResponseEntity<List<Long>> listChatsForCurrentUser() {
     return ResponseEntity.ok(chatService.listChatsForCurrentUser()).getBody();
 }

 /**
  * Отправка сообщения другому пользователю
  */
 @PostMapping("/message")
 public ResponseEntity<MessageDTO> sendMessage(@RequestBody MessageDTO messageDto) {
     MessageDTO savedMessage = chatService.sendMessage(messageDto).getBody();
     messagingTemplate.convertAndSend("/topic/public/" + savedMessage.getReceiver(), savedMessage);
     return ResponseEntity.created(null).body(savedMessage);
 }

 /**
  * История сообщений для определенного чата
  */
 @GetMapping("/history/{chatId}")
 public ResponseEntity<List<MessageDTO>> getChatHistory(@PathVariable Long chatId) {
     return ResponseEntity.ok(chatService.getChatHistory(chatId)).getBody();
 }
}
