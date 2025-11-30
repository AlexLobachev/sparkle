package com.example.sparkle.sparkle.dto.chat;

import com.example.sparkle.sparkle.dto.user.UserMatchDto;
import com.example.sparkle.sparkle.model.Chat;
import com.example.sparkle.sparkle.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Getter
@Setter
@Builder
public class ChatDtoGet {
    private Long chatId;
    private LocalDateTime sentAt;
    private UserMatchDto user;


    public static ChatDtoGet toChatDtoList(Chat chat, Long currentUserId){
        List<User> usersChat = Arrays.asList(chat.getSender(),chat.getReceiver());
        User otherUser = usersChat.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Чат не содержит собеседника (оба участника — текущий пользователь?)"));
        return ChatDtoGet.builder()
                .chatId(chat.getId())
                .sentAt(chat.getSentAt())
                .user(UserMatchDto.toUserMatchDto(otherUser))
                .build();
    }



}
