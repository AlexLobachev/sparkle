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
    private Boolean matchStatus = false;




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match match1)) return false;
        return getId().equals(match1.getId()) && getFirstUser().equals(match1.getFirstUser()) && getSecondUser().equals(match1.getSecondUser()) && getCreatedAt().equals(match1.getCreatedAt()) && getMatchStatus().equals(match1.getMatchStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstUser(), getSecondUser(), getCreatedAt(), getMatchStatus());
    }
}
