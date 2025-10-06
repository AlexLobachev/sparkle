package com.example.sparkle.sparkle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "matches")
@Getter
@Setter
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
    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match match)) return false;
        return getId().equals(match.getId()) && getFirstUser().equals(match.getFirstUser()) && getSecondUser().equals(match.getSecondUser()) && getCreatedAt().equals(match.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstUser(), getSecondUser(), getCreatedAt());
    }
}
