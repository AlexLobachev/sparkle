package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    // List<Chat> findAllBySenderIdOrReceiverId(Long senderId, Long receiverId);

    @Query(value = """
            SELECT *
            FROM chats c
            WHERE
                (c.sender_id = :senderId OR c.receiver_id = :receiverId)
              AND NOT EXISTS (
                    SELECT 1
                    FROM deleted_chats dc
                    WHERE dc.chat_id = c.id
                      AND dc.user_id = :senderId
                );
            """, nativeQuery = true)
    List<Chat> findAllBySenderIdOrReceiverId(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);


    @Query(value = """
            SELECT *
            FROM chats c
            WHERE
                ((c.sender_id = :senderId OR c.receiver_id = :senderId) AND c.id = :chatId)
              AND NOT EXISTS (
                    SELECT 1
                    FROM deleted_chats dc
                    WHERE dc.chat_id = c.id
                      AND dc.user_id = :senderId
                );
            """, nativeQuery = true)
    Chat findBySenderIdOrReceiverIdAndChatId(
            @Param("senderId") Long senderId,
            @Param("chatId") Long chatId);
}
