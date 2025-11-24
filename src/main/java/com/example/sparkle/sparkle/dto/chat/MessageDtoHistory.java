package com.example.sparkle.sparkle.dto.chat;

import com.example.sparkle.sparkle.dto.user.UserMessageDto;
import com.example.sparkle.sparkle.model.ChatMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
public class MessageDtoHistory {
    private Long id;
    private UserMessageDto sender;
    private String content;
    private LocalDateTime sentAt;
    public static MessageDtoHistory  toMessageDto(ChatMessage chatMessage){
        return MessageDtoHistory.builder()
                        .id(chatMessage.getId())
                        .content(chatMessage.getContent())
                        .sentAt(chatMessage.getSentAt())
                        .sender(UserMessageDto.builder()
                                        .id(chatMessage.getSender().getId())
                                        .username(chatMessage.getSender().getUsername())
                                        .build())
                        .build();

    }

   }
