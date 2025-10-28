package com.example.sparkle.sparkle.dto;

import com.example.sparkle.sparkle.dto.user.UserMatchDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchDto {
    private Long matchId;
    private UserMatchDto user;
}
