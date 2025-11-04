package com.example.sparkle.sparkle.model;

import com.example.sparkle.sparkle.dto.ChatDtoList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "chats")
@Getter
@Setter
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private User sender;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private User receiver;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="sent_at")
    private LocalDateTime sentAt;
    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    public static ChatDtoList toChatDtoList(Chat chat){
        ChatDtoList chatDtoList  = new ChatDtoList();
        chatDtoList.setChatId(chat.getId());
        chatDtoList.setSentAt(chat.getSentAt());
        List<Long> interlocutors = Arrays.asList(chat.getSender().getId(),chat.getReceiver().getId());
        chatDtoList.setInterlocutors(interlocutors);
        return chatDtoList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chat)) return false;
        Chat chat = (Chat) o;
        return Objects.equals(getId(), chat.getId()) && Objects.equals(getSender(), chat.getSender()) && Objects.equals(getReceiver(), chat.getReceiver()) && Objects.equals(getSentAt(), chat.getSentAt()) && Objects.equals(getChatMessages(), chat.getChatMessages());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSender(), getReceiver(), getSentAt(), getChatMessages());
    }
}
