package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllById(Long chatId);

    List<ChatMessage> findAllByChatIdAndBasket(Long chatId, Boolean basket);

    @Modifying
    @Query(value = """
            UPDATE messages 
            SET basket = :basket 
            WHERE id = :id
            """, nativeQuery = true)
    int updateBasket(Boolean basket, Long id);
}
