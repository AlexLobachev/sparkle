package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.chat.ChatDtoGet;
import com.example.sparkle.sparkle.dto.chat.MessageDtoHistory;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.Chat;
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

    MessageDtoHistory sendMessage(ChatMessage message);

    /**
     * История сообщений для определенного чата
     */

    List<MessageDtoHistory> getChatHistory(Long chatId);

    /**
     * Создание нового чата
     */

    ChatDtoGet createChat(Long receiverId);

    /**
     * Удаление чата для одного пользователя
     */

    void deleteChat(Long chatId);

    void deleteMessage(Long messageId);

    /**
     * Получение чата по id и id пользователя (для удаления)
     */

    Chat getChatByReceiverIdAndSenderId(Long userId1, Long userId2);
    /**
     * Получение чата по id
     */

    public Chat getChatById(Long chatId);

}
