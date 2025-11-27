package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.ChatDelete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeletedChatsRepository extends JpaRepository <ChatDelete,Long> {
    List<ChatDelete> findAllByUserId(Long userId);
    ChatDelete findByUserIdAndChatId(Long userId,Long chatId);
    ChatDelete findByUserIdAndAndChatId(Long userId,Long chatId);
    void deleteByUserIdAndChatId(Long userId, Long chatId);
    @Query("select chatId from ChatDelete where userId = ?1")
    List<Long> findAllChatId(Long userId);
}
