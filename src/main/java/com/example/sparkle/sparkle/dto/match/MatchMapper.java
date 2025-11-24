package com.example.sparkle.sparkle.dto.match;

import com.example.sparkle.sparkle.dto.user.UserMatchDto;
import com.example.sparkle.sparkle.model.Match;
import com.example.sparkle.sparkle.model.User;
import lombok.Builder;

@Builder
public class MatchMapper {
    public static MatchDto toMathDto(Match match) {
        if (match != null) {
            return MatchDto.builder()
                    .matchId(match.getId())
                    .user(UserMatchDto.toUserMatchDto(match.getSecondUser()))
                    .build();

        }
        return null;
    }

    public static MatchDto toMathDto(User user) {
        if (user != null) {
            return MatchDto.builder()
                    .matchId(user.getId())
                    .user(UserMatchDto.toUserMatchDto(user))
                    .build();

        }
        return null;
    }


}
