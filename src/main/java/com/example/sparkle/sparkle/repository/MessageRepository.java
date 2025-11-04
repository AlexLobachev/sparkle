package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<ChatMessage,Long> {
    List<ChatMessage> findAllById(Long chatId);
    List<ChatMessage> findAllByChatId(Long chatId);
}
