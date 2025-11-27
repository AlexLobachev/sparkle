package com.example.sparkle.sparkle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "deleted_chats")
@IdClass(UserKey.class)
@Getter
@Setter
public class ChatDelete {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Id
    @Column(name = "chat_id")
    private Long chatId;
    private LocalDateTime deleted_at = LocalDateTime.now();

}
