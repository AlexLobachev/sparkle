package com.example.sparkle.sparkle.dto.message;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChatMessageDto {
    private Long userId;
    private String username;
}
