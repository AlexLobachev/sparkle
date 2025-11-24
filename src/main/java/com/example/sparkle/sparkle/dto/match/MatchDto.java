package com.example.sparkle.sparkle.dto.match;

import com.example.sparkle.sparkle.dto.user.UserMatchDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MatchDto {
    private Long matchId;
    private UserMatchDto user;


}
