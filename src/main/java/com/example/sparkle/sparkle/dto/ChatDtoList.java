package com.example.sparkle.sparkle.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class ChatDtoList {
    private Long chatId;
    private LocalDateTime sentAt;
    private List<Long> interlocutors;

}
