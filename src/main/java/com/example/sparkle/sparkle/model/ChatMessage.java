package com.example.sparkle.sparkle.model;

import com.example.sparkle.sparkle.dto.MessageDtoHistory;
import com.example.sparkle.sparkle.dto.user.UserMessageDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "messages")
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private User sender;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "message_status")
    private Boolean messageStatus;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;


    public static MessageDtoHistory toMessageDtoHistory(ChatMessage chatMessage){
        MessageDtoHistory messageDtoHistory = new MessageDtoHistory();
        messageDtoHistory.setIdMessage(chatMessage.getId());
        messageDtoHistory.setContent(chatMessage.getContent());
        messageDtoHistory.setSentAt(chatMessage.getSentAt());
        messageDtoHistory.setSender(toUserMessageDto(chatMessage));
        return messageDtoHistory;
    }
    public static UserMessageDto toUserMessageDto(ChatMessage chatMessage){
        UserMessageDto userMessageDto = new UserMessageDto();
        userMessageDto.setUserName(chatMessage.getSender().getUsername());
        userMessageDto.setUserId(chatMessage.getSender().getId());
        return userMessageDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage that)) return false;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getContent(), that.getContent()) && Objects.equals(getSender(), that.getSender()) && Objects.equals(getSentAt(), that.getSentAt()) && getMessageStatus() == that.getMessageStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getContent(), getSender(), getSentAt(), getMessageStatus());
    }
}
