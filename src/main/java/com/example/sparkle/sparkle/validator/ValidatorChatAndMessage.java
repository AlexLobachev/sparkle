package com.example.sparkle.sparkle.validator;

import com.example.sparkle.sparkle.exception.Forbidden;
import com.example.sparkle.sparkle.exception.NoContent;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.Chat;
import com.example.sparkle.sparkle.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ValidatorChatAndMessage {

    public void chatForbidden(Chat chat, Long userId) {
        if (!chat.getSender().getId().equals(userId) && !chat.getReceiver().getId().equals(userId)) {
            log.warn("Доступ запрещен, у вас нет прав доступа к этому чату");
            throw new Forbidden("Доступ запрещен, у вас нет прав доступа к этому чату");

        }

    }

    public void chatForbiddenList(List<Chat> chats, Long userId) {
        if (chats.stream()
                .noneMatch(user -> user.getReceiver().getId().equals(userId) ||
                        user.getSender().getId().equals(userId))) {
            log.warn("Доступ запрещен, у вас нет прав доступа к спискам чатов");
            throw new Forbidden("Доступ запрещен, у вас нет прав доступа к спискам чатов");

        }

    }

    public void chatNotFound(Chat chat) {
        if (chat == null) {
            log.warn("Чат не найден");
            throw new NotFound("Чат не найден");
        }

    }

    public void chatNoContent(List<Chat> chats) {
        if (chats.isEmpty()) {
            log.info("У пользователя нет чатов");
            throw new NoContent();
        }

    }
    public void messageNoContent(List<ChatMessage> chatMessages) {
        if (chatMessages.isEmpty()) {
            log.info("История переписки не найдена");
            throw new NoContent();
        }

    }

}
