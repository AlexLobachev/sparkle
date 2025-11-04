package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.ChatDelete;
import com.example.sparkle.sparkle.model.UserChatKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeletedChatsRepository extends JpaRepository <ChatDelete,Long> {
    List<ChatDelete> findAllByUserId(Long userId);
    ChatDelete findByUserId(Long userId);
    ChatDelete findByUserIdAndAndChatId(Long userId,Long chatId);
    void deleteByUserIdAndChatId(Long userId, Long chatId);
}
