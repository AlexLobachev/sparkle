package com.example.sparkle.sparkle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Сущность для отслеживания удалённых чатов пользователем.
 * Составной ключ: user_id + chat_id.
 */
@Entity
@Table(name = "deleted_chats")
@IdClass(UserKey.class)
@Getter
@Setter
public class ChatDelete {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt = LocalDateTime.now();
}