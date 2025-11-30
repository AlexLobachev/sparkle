package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    // List<Chat> findAllBySenderIdOrReceiverId(Long senderId, Long receiverId);

    @Query(value = """            
                        
            SELECT *
            FROM chats
            WHERE (
                (sender_id = :senderId AND receiver_id != :receiverId)
                OR
                (receiver_id = :senderId AND sender_id != :receiverId)
            )
            AND id NOT IN (:ids)
                        
            """, nativeQuery = true)
    List<Chat> findAllBySenderIdAndReceiverId(@Param("senderId") Long senderId,
                                              @Param("receiverId") Long receiverId,
                                              @Param("ids") List<Long> ids);

    @Query(value = """            
                        
            SELECT *
            FROM chats
            WHERE (
                (sender_id = :senderId AND receiver_id != :receiverId)
                OR
                (receiver_id = :senderId AND sender_id != :receiverId) 
                ) 
            
            """, nativeQuery = true)
    List<Chat> findAllBySenderIdAndReceiverId(@Param("senderId") Long senderId,
                                              @Param("receiverId") Long receiverId);



    @Query(value = """
    SELECT *
    FROM chats
    WHERE (
        (sender_id = :userId AND receiver_id != :userId)  -- Я отправил не себе
        OR
        (receiver_id = :userId AND sender_id != :userId)  -- Мне отправили не от меня
    )
    
    """, nativeQuery = true)
    List<Chat> findChatsWhereUserIsParticipant(
            @Param("userId") Long userId,
            @Param("excludedChatIds") List<Long> excludedChatIds
    );

    //AND id NOT IN (:excludedChatIds)
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

    void deleteByReceiverIdAndSenderId(Long receiverId, Long senderId);


    @Query(value = """
            SELECT *
            FROM chats
            WHERE (sender_id = :userId1 AND receiver_id = :userId2)
               OR (sender_id = :userId2 AND receiver_id = :userId1)
            """, nativeQuery = true)
    Optional<Chat> getChatByReceiverIdAndSenderId(Long userId1, Long userId2);

}

