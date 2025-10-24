package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.MessageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ChatServiceImpl implements ChatService{
    /**
     * Отображение списка чатов текущего пользователя
     */

    public ResponseEntity<List<Long>> listChatsForCurrentUser() {
        return null;
    }

    /**
     * Отправка сообщения другому пользователю
     */

    public ResponseEntity<MessageDTO> sendMessage(MessageDTO messageDto) {
        return null;
    }

    /**
     * История сообщений для определенного чата
     */

    public ResponseEntity<List<MessageDTO>> getChatHistory(Long chatId) {
        return null;
    }
}
