package com.example.sparkle.sparkle.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class UserChatKey implements Serializable {
    public Long userId;
    public Long chatId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserChatKey that)) return false;
        return Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getChatId(), that.getChatId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getChatId());
    }
}
