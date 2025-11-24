package com.example.sparkle.sparkle.dto.chat;

import com.example.sparkle.sparkle.dto.user.UserMatchDto;
import com.example.sparkle.sparkle.model.Chat;
import com.example.sparkle.sparkle.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Getter
@Setter
@Builder
public class ChatDtoGet {
    private Long chatId;
    private LocalDateTime sentAt;
    private List<UserMatchDto> users = new ArrayList<>();


    public static ChatDtoGet toChatDtoList(Chat chat){
        List<User> usersChat = Arrays.asList(chat.getSender(),chat.getReceiver());
        List <UserMatchDto> userMatchDtoList = new ArrayList<>();
        usersChat.forEach(user -> userMatchDtoList.add(UserMatchDto.toUserMatchDto(user)));

        return ChatDtoGet.builder()
                .chatId(chat.getId())
                .sentAt(chat.getSentAt())
                .users(userMatchDtoList)
                .build();
    }

}
