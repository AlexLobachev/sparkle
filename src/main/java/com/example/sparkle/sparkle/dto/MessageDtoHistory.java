package com.example.sparkle.sparkle.dto;

import com.example.sparkle.sparkle.dto.user.UserMessageDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class MessageDtoHistory {
    private Long idMessage;
    private UserMessageDto sender;
    private String content;
    private LocalDateTime sentAt;
   }
