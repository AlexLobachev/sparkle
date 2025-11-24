package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.chat.ChatDtoGet;
import com.example.sparkle.sparkle.dto.chat.MessageDtoHistory;
import com.example.sparkle.sparkle.model.Chat;
import com.example.sparkle.sparkle.model.ChatDelete;
import com.example.sparkle.sparkle.model.ChatMessage;

import java.util.List;

public interface ChatService {
    /**
     * Отображение списка чатов текущего пользователя
     */

    List<ChatDtoGet> listChatsForCurrentUser();

    /**
     * Отправка сообщения другому пользователю
     */

    MessageDtoHistory sendMessage(ChatMessage savedMessage);

    /**
     * История сообщений для определенного чата
     */

    List<ChatMessage> getChatHistory(Long chatId);

    /**
     * Создание нового чата
     */

    Chat createChat(Long senderId, Long receiverId);

    /**
     * Удаление чата для одного пользователя
     */

    public ChatDelete deleteChat(Long userId, Long chatId);
}
