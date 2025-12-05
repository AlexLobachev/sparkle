package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.chat.*;
import com.example.sparkle.sparkle.exception.BadRequest;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.*;
import com.example.sparkle.sparkle.repository.*;
import com.example.sparkle.sparkle.validator.ValidatorChatAndMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Реализация сервиса чата.
 * Основные функции:
 * - Создание чата
 * - Отправка сообщений
 * - Получение истории
 * - Управление чатами и сообщениями
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ValidatorChatAndMessage validatorChatAndMessage;
    private final DeletedChatsRepository deletedChatsRepository;
    private final UserRepository userRepository;

    /**
     * Создаёт чат между текущим пользователем и получателем.
     * Если чат уже был, но удалён — восстанавливает его.
     */
    @Transactional
    @Override
    public ChatDtoGet createChat(Long receiverId) {
        User sender = getCurrentUser();
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new NotFound("Пользователь не найден"));

        Chat chat = chatRepository.getChatByReceiverIdAndSenderId(sender.getId(), receiverId)
                .orElse(new Chat());

        if (chat.getId() != null) {
            ChatDelete chatDelete = deletedChatsRepository.findByUserIdAndChatId(sender.getId(), chat.getId());
            if (chatDelete != null) {
                deletedChatsRepository.deleteByUserIdAndChatId(chatDelete.getUserId(), chatDelete.getChatId());
            }
            return ChatDtoGet.toChatDtoList(chat, sender.getId());
        }

        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setSentAt(LocalDateTime.now());
        chat = chatRepository.save(chat);
        return ChatDtoGet.toChatDtoList(chat, receiverId);
    }

    /**
     * Отправляет сообщение в чат.
     * Рассылает сообщение через WebSocket (/topic/chat.{id}).
     */

    @Override
    @Transactional
    public MessageDtoHistory sendMessage(ChatMessage message) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Пользователь не аутентифицирован");
        }

        // ✅ Безопасное извлечение имени пользователя
        Object principal = auth.getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
        }

        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFound("Пользователь не найден"));

        Chat chat = chatRepository.findById(message.getChat().getId())
                .orElseThrow(() -> new NotFound("Чат не найден"));

        validatorChatAndMessage.chatForbidden(chat, sender.getId());

        message.setSender(sender);
        message.setSentAt(LocalDateTime.now());
        message.setChat(chat);
        ChatMessage savedMessage = messageRepository.save(message);

        // Формируем DTO
        ChatMessageDtoSent dto = ChatMessageDtoSent.toChatMessageDtoSent(savedMessage);
        dto.setSender(UserChatDto.toUserChatDto(sender));

        // Отправляем через WebSocket
        messagingTemplate.convertAndSend("/topic/chat." + message.getChat().getId(), dto);

        return MessageDtoHistory.toMessageDto(savedMessage);
    }

    /**
     * Возвращает список чатов текущего пользователя.
     */
    @Override
    public List<ChatDtoGet> listChatsForCurrentUser() {
        User user = getCurrentUser();
        List<Long> deletedChatIds = deletedChatsRepository.findAllChatId(user.getId());
        List<Chat> chats = chatRepository.findChatsWhereUserIsParticipant(user.getId(), deletedChatIds);
        validatorChatAndMessage.chatNoContent(chats);

        return chats.stream()
                .map(chat -> ChatDtoGet.toChatDtoList(chat, user.getId()))
                .toList();
    }

    /**
     * Возвращает чат по ID.
     */
    @Override
    public Chat getChatById(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFound("Чат не найден"));
    }

    /**
     * Возвращает историю сообщений для чата.
     */
    @Override
    public List<MessageDtoHistory> getChatHistory(Long chatId) {
        User user = getCurrentUser();
        Chat chat = getChatById(chatId);
        validatorChatAndMessage.chatForbidden(chat, user.getId());

        List<ChatMessage> messages = messageRepository.findAllByChatIdAndBasket(chatId, false);
        validatorChatAndMessage.messageNoContent(messages);

        return messages.stream()
                .map(MessageDtoHistory::toMessageDto)
                .toList();
    }

    /**
     * Удаляет чат для текущего пользователя (помечает как удалённый).
     */
    @Transactional
    @Override
    public void deleteChat(Long chatId) {
        Chat chat = getChatById(chatId);
        User user = getCurrentUser();
        validatorChatAndMessage.chatForbidden(chat, user.getId());

        ChatDelete chatDelete = new ChatDelete();
        chatDelete.setChatId(chatId);
        chatDelete.setUserId(user.getId());
        deletedChatsRepository.save(chatDelete);
    }

    /**
     * Удаляет сообщение (помечает как удалённое).
     */
    @Transactional
    @Override
    public void deleteMessage(Long messageId) {
        if (!messageRepository.existsById(messageId)) {
            throw new NotFound("Сообщение не найдено");
        }
        int updated = messageRepository.updateBasket(true, messageId);
        if (updated == 0) {
            throw new BadRequest("Не удалось удалить сообщение");
        }
    }

    /**
     * Получает текущего аутентифицированного пользователя.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
    }

    /**
     * Получение чата по id и id пользователя (для удаления)
     */

    public Chat getChatByReceiverIdAndSenderId(Long userId1, Long userId2){
        return chatRepository.getChatByReceiverIdAndSenderId(userId1, userId2).orElse(null);
    }
}