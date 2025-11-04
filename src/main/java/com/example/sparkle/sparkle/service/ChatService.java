package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.ChatDtoList;
import com.example.sparkle.sparkle.model.Chat;
import com.example.sparkle.sparkle.model.ChatDelete;
import com.example.sparkle.sparkle.model.ChatMessage;

import java.util.List;

public interface ChatService {
    /**
     * Отображение списка чатов текущего пользователя
     */

    List<ChatDtoList> listChatsForCurrentUser(Long userId);

    /**
     * Отправка сообщения другому пользователю
     */

    ChatMessage sendMessage(Long senderId,Long chatId, ChatMessage chatMessageDto);

    /**
     * История сообщений для определенного чата
     */

    List<ChatMessage> getChatHistory(Long userId, Long chatId);

    /**
     * Создание нового чата
     */

    Chat createChat(Long senderId, Long receiverId);

    /**
     * Удаление чата для одного пользователя
     */

    public ChatDelete deleteChat(Long userId, Long chatId);
}
