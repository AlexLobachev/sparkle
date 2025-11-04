package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.ChatDtoList;
import com.example.sparkle.sparkle.exception.BadRequest;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.Chat;
import com.example.sparkle.sparkle.model.ChatDelete;
import com.example.sparkle.sparkle.model.ChatMessage;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.repository.ChatRepository;
import com.example.sparkle.sparkle.repository.DeletedChatsRepository;
import com.example.sparkle.sparkle.repository.MessageRepository;
import com.example.sparkle.sparkle.validator.ValidatorChatAndMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final MatchService matchService;

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ValidatorChatAndMessage validatorChatAndMessage;
    private final DeletedChatsRepository deletedChatsRepository;

    @Autowired
    public ChatServiceImpl(SimpMessagingTemplate messagingTemplate,
                           UserService userService,
                           MatchService matchService,
                           MessageRepository messageRepository,
                           ValidatorChatAndMessage validatorChatAndMessage,
                           ChatRepository chatRepository,
                           DeletedChatsRepository deletedChatsRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.matchService = matchService;
        this.messageRepository = messageRepository;
        this.validatorChatAndMessage = validatorChatAndMessage;
        this.chatRepository = chatRepository;
        this.deletedChatsRepository = deletedChatsRepository;
    }

    /**
     * Создание нового чата
     */
    @Transactional
    public Chat createChat(Long senderId, Long receiverId) {
        Chat chat = new Chat();
        matchService.getCurrentMatches(senderId)
                .stream()
                .filter(f -> f.getSecondUser().getId().equals(receiverId))
                .findFirst()
                .orElseThrow(() -> new NotFound("Метч еще не создан"));

        User sender = userService.getUserById(senderId)
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
        User receiver = userService.getUserById(receiverId)
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
        ChatDelete chatDelete = deletedChatsRepository.findByUserId(senderId);
        if (chatDelete != null) {
            deletedChatsRepository.deleteByUserIdAndChatId(chatDelete.getUserId(), chatDelete.getChatId());
            return chatRepository.findById(chatDelete.getChatId()).orElseThrow(() -> new NotFound("Чат не найден"));
        }
        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setSentAt(LocalDateTime.now());
        try {
            chat = chatRepository.save(chat);
        }
        catch (DataIntegrityViolationException e){
            throw new BadRequest("Чат уже создан ранее");
        }
        return chat;
    }

    /**
     * Отправка сообщения другому пользователю
     */
    @Transactional
    public ChatMessage sendMessage(Long senderId, Long chatId, ChatMessage savedMessage) {
        Chat chat = chatRepository.findBySenderIdOrReceiverIdAndChatId(senderId, chatId);
        validatorChatAndMessage.chatNotFound(chat);
        User sender = userService.getUserById(senderId)
                .orElseThrow(() -> new NotFound("Пользователь не найден"));

        matchService.getCurrentMatches(chat.getSender().getId())
                .stream()
                .filter(f -> f.getSecondUser().getId().equals(chat.getReceiver().getId()))
                .findFirst()
                .orElseThrow(() -> new NotFound("Метч еще не создан"));
        validatorChatAndMessage.chatNotFound(chat);
        validatorChatAndMessage.chatForbidden(chat, senderId);


        savedMessage.setSentAt(LocalDateTime.now());
        savedMessage.setSender(sender);
        savedMessage.setChat(chat);


        messagingTemplate.convertAndSend(
                "/topic/public/" + chat.getReceiver().getId(),
                savedMessage
        );
        messageRepository.save(savedMessage);

        return savedMessage;
    }


    /**
     * Отображение списка чатов текущего пользователя
     */

    public List<ChatDtoList> listChatsForCurrentUser(Long userId) {
        userService.getUserById(userId)
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
        List<Chat> chats = chatRepository.findAllBySenderIdOrReceiverId(userId, userId);
        validatorChatAndMessage.chatNoContent(chats);

        return chats.stream().map(Chat::toChatDtoList).toList();
    }


    /**
     * История сообщений для определенного чата
     */

    public List<ChatMessage> getChatHistory(Long userId, Long chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFound("Чат не найден"));
        validatorChatAndMessage.chatNotFound(chat);
        validatorChatAndMessage.chatForbidden(chat, userId);
        List<ChatMessage> chatMessagesHistory = messageRepository.findAllByChatId(chatId);
        validatorChatAndMessage.messageNoContent(chatMessagesHistory);
        return chatMessagesHistory;
    }

    /**
     * Удаление чата для одного пользователя
     */
    public ChatDelete deleteChat(Long userId, Long chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFound("Чат не найден"));
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
        validatorChatAndMessage.chatNotFound(chat);
        validatorChatAndMessage.chatForbidden(chat, userId);
        ChatDelete chatDelete = new ChatDelete();
        chatDelete.setChatId(chatId);
        chatDelete.setUserId(userId);
        return deletedChatsRepository.save(chatDelete);
    }
    /**
     * Удаление чата для обоих пользователей
     * Когда пользователь удаляет метч, считается за блокировку, и чаты удаляются
     */
    public ChatDelete deleteChatAndBlock(Long userId, Long chatId) {

        return null;
    }
}
