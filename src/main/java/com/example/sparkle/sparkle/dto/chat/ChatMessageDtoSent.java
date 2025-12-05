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
    private UserChatDto sender;
    private UserChatDto receiver;
    private Long chatId;
    private LocalDateTime sentAt;

    public static ChatMessageDtoSent toChatMessageDtoSent(ChatMessage chatMessage) {
        return ChatMessageDtoSent.builder()
                .id(chatMessage.getId())
                .content(chatMessage.getContent())
                .sender(UserChatDto.toUserChatDto(chatMessage.getChat().getSender()))
                .receiver(UserChatDto.toUserChatDto(chatMessage.getChat().getReceiver()))
                .chatId(chatMessage.getChat().getId())
                .sentAt(chatMessage.getSentAt())
                .build();


    }
}


