package com.example.sparkle.sparkle.model;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

/**
 * Составной ключ для сущности ChatDelete (user_id + chat_id).
 */
@Getter
@Setter
public class UserKey implements Serializable {
    private Long userId;
    private Long chatId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserKey that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(chatId, that.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, chatId);
    }
}