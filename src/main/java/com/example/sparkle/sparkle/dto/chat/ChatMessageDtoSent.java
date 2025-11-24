package com.example.sparkle.sparkle.dto.chat;

import com.example.sparkle.sparkle.model.ChatMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatMessageDtoSent {
    private Long id;
    private String content;
    private Long senderId;
    private Long receiverId;
    private Long chatId;
    private LocalDateTime sentAt;

    public static ChatMessageDtoSent toCatMessageDtoSent(ChatMessage chatMessage) {
        return ChatMessageDtoSent.builder()
                .id(chatMessage.getId())
                .content(chatMessage.getContent())
                .senderId(chatMessage.getChat().getSender().getId())
                .receiverId(chatMessage.getChat().getReceiver().getId())
                .sentAt(chatMessage.getSentAt())
                .build();


    }
}


