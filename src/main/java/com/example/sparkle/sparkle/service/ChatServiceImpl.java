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
import org.springframework.dao.DataIntegrityViolationException;
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
    private final UserService userService;
    private final MatchService matchService;

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ValidatorChatAndMessage validatorChatAndMessage;
    private final DeletedChatsRepository deletedChatsRepository;
    private final UserRepository userRepository;

    @Autowired
    public ChatServiceImpl(SimpMessagingTemplate messagingTemplate,
                           UserService userService,
                           MatchService matchService,
                           MessageRepository messageRepository,
                           ValidatorChatAndMessage validatorChatAndMessage,
                           ChatRepository chatRepository,
                           DeletedChatsRepository deletedChatsRepository,
                           UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.matchService = matchService;
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
    public Chat createChat(Long senderId, Long receiverId) {
        Chat chat = new Chat();
        //matchService.getCurrentMatches(senderId)
        //        .stream()
        //        .filter(f -> f.getSecondUser().getId().equals(receiverId))
        //        .findFirst()
        //        .orElseThrow(() -> new NotFound("Метч еще не создан"));

        User sender = userRepository.findById(senderId).orElseThrow(() -> new NotFound("Пользователь не найден"));
        User receiver = userRepository.findById(receiverId)
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
        } catch (DataIntegrityViolationException e) {
            throw new BadRequest("Чат уже создан ранее");
        }
        return chat;
    }

    /**
     * Отправка сообщения другому пользователю
     */
    @Transactional
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

    public List<ChatDtoGet> listChatsForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByUsername(
                currentUser.getUsername()).orElseThrow(() -> new NotFound("Пользователь не найден"));
        log.debug("чаты пользователя {}", chatRepository.findAllBySenderIdOrReceiverId(user.getId(), user.getId()));
        List<Chat> chats = chatRepository.findAllBySenderIdOrReceiverId(user.getId(), user.getId());
        validatorChatAndMessage.chatNoContent(chats);

        return chatRepository.findAllBySenderIdOrReceiverId(user.getId(), user.getId()).stream().map(ChatDtoGet::toChatDtoList).toList();
    }


    /**
     * История сообщений для определенного чата
     */

    public List<ChatMessage> getChatHistory(Long chatId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByUsername(
                currentUser.getUsername()).orElseThrow(() -> new NotFound("Пользователь не найден"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFound("Чат не найден"));
        validatorChatAndMessage.chatNotFound(chat);
        validatorChatAndMessage.chatForbidden(chat, user.getId());
        List<ChatMessage> chatMessagesHistory = messageRepository.findAllByChatId(chatId);
        validatorChatAndMessage.messageNoContent(chatMessagesHistory);
        return chatMessagesHistory;
    }

    /**
     * Удаление чата для одного пользователя
     */
    public ChatDelete deleteChat(Long userId, Long chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new NotFound("Чат не найден"));
        User user = userRepository.findById(userId)
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
