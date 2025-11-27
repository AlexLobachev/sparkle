package com.example.sparkle.sparkle.dto.chat;

import com.example.sparkle.sparkle.dto.message.ChatMessageDto;
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
    private ChatMessageDto sender;
    private String content;
    private LocalDateTime sentAt;

    public static MessageDtoHistory toMessageDto(ChatMessage chatMessage) {
        return MessageDtoHistory.builder()
                .id(chatMessage.getId())
                .content(chatMessage.getContent())
                .sentAt(chatMessage.getSentAt())
                .sender(ChatMessageDto.builder()
                        .userId(chatMessage.getSender().getId())
                        .username(chatMessage.getSender().getUsername())
                        .build())
                .build();

    }

}
