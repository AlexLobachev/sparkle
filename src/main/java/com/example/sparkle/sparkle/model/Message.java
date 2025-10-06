package com.example.sparkle.sparkle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "messages")
@Getter
@Setter
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(/*columnDefinition = "TEXT"*/)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match")
    private Match match;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="sent_at")
    private LocalDateTime sentAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message message)) return false;
        return getId().equals(message.getId()) && getContent().equals(message.getContent()) && getSender().equals(message.getSender()) && getReceiver().equals(message.getReceiver()) && getMatch().equals(message.getMatch()) && getSentAt().equals(message.getSentAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getContent(), getSender(), getReceiver(), getMatch(), getSentAt());
    }
}
