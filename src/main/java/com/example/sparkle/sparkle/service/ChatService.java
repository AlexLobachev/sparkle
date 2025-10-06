package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.MessageDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ChatService {
    /**
     * Отображение списка чатов текущего пользователя
     */

    ResponseEntity<List<Long>> listChatsForCurrentUser();

    /**
     * Отправка сообщения другому пользователю
     */

    ResponseEntity<MessageDTO> sendMessage(MessageDTO messageDto);

    /**
     * История сообщений для определенного чата
     */

    ResponseEntity<List<MessageDTO>> getChatHistory(Long chatId);
}
