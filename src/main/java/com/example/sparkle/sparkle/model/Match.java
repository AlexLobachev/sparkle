package com.example.sparkle.sparkle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "matches")
@Getter
@Setter
@Slf4j
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_user_id")
    private User firstUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_user_id")
    private User secondUser;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "match_status")
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    public enum MatchStatus {
        LIKE, DISLIKE, MATCHED
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match match)) return false;
        return Objects.equals(getId(), match.getId()) && Objects.equals(getFirstUser(), match.getFirstUser()) && Objects.equals(getSecondUser(), match.getSecondUser()) && Objects.equals(getCreatedAt(), match.getCreatedAt()) && getMatchStatus() == match.getMatchStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstUser(), getSecondUser(), getCreatedAt(), getMatchStatus());
    }
}
