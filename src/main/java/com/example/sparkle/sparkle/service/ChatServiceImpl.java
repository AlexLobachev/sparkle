package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.chat.ChatDtoGet;
import com.example.sparkle.sparkle.dto.chat.ChatMessageDtoSent;
import com.example.sparkle.sparkle.dto.chat.MessageDtoHistory;
import com.example.sparkle.sparkle.exception.BadRequest;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.Chat;
import com.example.sparkle.sparkle.model.ChatDelete;
import com.example.sparkle.sparkle.model.ChatMessage;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.repository.ChatRepository;
import com.example.sparkle.sparkle.repository.DeletedChatsRepository;
import com.example.sparkle.sparkle.repository.MessageRepository;
import com.example.sparkle.sparkle.repository.UserRepository;
import com.example.sparkle.sparkle.validator.ValidatorChatAndMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ValidatorChatAndMessage validatorChatAndMessage;
    private final DeletedChatsRepository deletedChatsRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChatServiceImpl(SimpMessagingTemplate messagingTemplate,
                           MessageRepository messageRepository,
                           ValidatorChatAndMessage validatorChatAndMessage,
                           ChatRepository chatRepository,
                           DeletedChatsRepository deletedChatsRepository,
                           UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.validatorChatAndMessage = validatorChatAndMessage;
        this.chatRepository = chatRepository;
        this.deletedChatsRepository = deletedChatsRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создание нового чата
     */
    @Transactional
    @Override
    public ChatDtoGet createChat(Long receiverId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User sender = userRepository.findByUsername(
                currentUser.getUsername()).orElseThrow(() -> new NotFound("Пользователь не найден"));

        Chat chat = chatRepository.getChatByReceiverIdAndSenderId(sender.getId(), receiverId).orElse(null);
        if (chat != null) {
            ChatDelete chatDelete = deletedChatsRepository.findByUserIdAndChatId(sender.getId(), chat.getId());
            if (chatDelete != null) {
                deletedChatsRepository.deleteByUserIdAndChatId(chatDelete.getUserId(), chatDelete.getChatId());
            }

            return ChatDtoGet.toChatDtoList(chat, sender.getId());
        }
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
        chat = new Chat();
        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setSentAt(LocalDateTime.now());
        chat = chatRepository.save(chat);
        return ChatDtoGet.toChatDtoList(chat, receiverId);
    }

    /**
     * Отправка сообщения другому пользователю
     */
    @Transactional
    @Override
    public MessageDtoHistory sendMessage(ChatMessage savedMessage) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByUsername(
                currentUser.getUsername()).orElseThrow(() -> new NotFound("Пользователь не найден"));
        Chat chat = chatRepository.findById(savedMessage.getChat().getId()).orElseThrow(() -> new NotFound("Чат не найден"));

        validatorChatAndMessage.chatForbidden(chat, user.getId());

        savedMessage.setSentAt(LocalDateTime.now());
        savedMessage.setSender(user);
        savedMessage.setChat(chat);


        messagingTemplate.convertAndSend(
                "/topic/public/" + chat.getReceiver().getId(),
                ChatMessageDtoSent.toCatMessageDtoSent(savedMessage)
        );
        messageRepository.save(savedMessage);
        return MessageDtoHistory.toMessageDto(savedMessage);
    }


    /**
     * Отображение списка чатов текущего пользователя
     */

    @Override
    public List<ChatDtoGet> listChatsForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));

        // Получаем ID удалённых чатов
        List<Long> deletedChatIds = deletedChatsRepository.findAllChatId(user.getId());


        // Если удалённых чатов нет — передаём пустой список
        List<Long> excludedIds = deletedChatIds.isEmpty() ? List.of() : deletedChatIds;

        // Ищем чаты, где пользователь — участник, но не ведёт диалог сам с собой
        List<Chat> chats = chatRepository.findChatsWhereUserIsParticipant(user.getId(), excludedIds);

        validatorChatAndMessage.chatNoContent(chats);

        return chats.stream()
                .map(chat -> ChatDtoGet.toChatDtoList(chat, user.getId()))
                .toList();
    }


    /**
     * История сообщений для определенного чата
     */
    @Override
    public List<MessageDtoHistory> getChatHistory(Long chatId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByUsername(
                currentUser.getUsername()).orElseThrow(() -> new NotFound("Пользователь не найден"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFound("Чат не найден"));
        validatorChatAndMessage.chatNotFound(chat);
        validatorChatAndMessage.chatForbidden(chat, user.getId());
        List<ChatMessage> chatMessagesHistory = messageRepository.findAllByChatIdAndBasket(chatId, false);
        validatorChatAndMessage.messageNoContent(chatMessagesHistory);
        return chatMessagesHistory.stream()
                .map(MessageDtoHistory::toMessageDto).toList();
    }

    /**
     * Удаление чата для одного пользователя
     */
    @Override
    @Transactional
    public void deleteChat(Long chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFound("Чат не найден"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByUsername(currentUser.getUsername()).orElseThrow(() -> new NotFound("Пользователь не найден"));
        validatorChatAndMessage.chatForbidden(chat, user.getId());
        ChatDelete chatDelete = new ChatDelete();
        chatDelete.setChatId(chatId);
        chatDelete.setUserId(user.getId());
        deletedChatsRepository.save(chatDelete);
    }


    /**
     * Получение чата по id и id пользователя (для удаления)
     */
    @Override
    public Chat getChatByReceiverIdAndSenderId(Long userId1, Long userId2) {
        return chatRepository.getChatByReceiverIdAndSenderId(userId1, userId2).orElseThrow(() -> new NotFound("Чат не найден"));
    }

    /**
     * Удаление сообщения, сообщение переносится в корзину
     */
    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        if (!messageRepository.existsById(messageId)) {
            throw new NotFound("Сообщение не найдено");
        }
        int status = messageRepository.updateBasket(true, messageId);
        if (status == 0) {
            throw new BadRequest("Ошибка при удалении сообщения");
        }

    }
}
