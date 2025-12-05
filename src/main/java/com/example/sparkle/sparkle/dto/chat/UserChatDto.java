package com.example.sparkle.sparkle.dto.chat;

import com.example.sparkle.sparkle.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserChatDto {
    private Long userId;
    private String username;
    private String photoUrl;

    public static UserChatDto toUserChatDto(User user) {

        return UserChatDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .photoUrl(user.getPhotos().stream().findFirst().get().getPhoto().getUrl())
                .build();
    }
}
