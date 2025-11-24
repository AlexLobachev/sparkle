package com.example.sparkle.sparkle.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserMessageDto {
    private Long id;
    private String username;
}
