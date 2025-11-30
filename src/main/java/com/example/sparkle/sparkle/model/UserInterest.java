package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Связь пользователя с интересами (многие-ко-многим).
 * Использует составную таблицу user_interests.
 */
@Getter
@Setter
@Entity
@Table(name = "user_interests", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "interest"}))
public class UserInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest", nullable = false)
    private Interest interest;


    public UserInterest() {

    }

    public UserInterest(Interest interest) {
        this.interest = interest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInterest that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    /**
     * Создаёт упрощённого пользователя из интереса (устаревшее?)
     */
    @Deprecated
    public static User toUser(UserInterest interest) {
        User user = new User();
        User source = interest.getUser();
        user.setId(source.getId());
        user.setUsername(source.getUsername());
        user.setGender(source.getGender());
        user.setPreferredGender(source.getPreferredGender());
        user.setBirthDate(source.getBirthDate());
        user.setEmail(source.getEmail());
        user.setAboutMe(source.getAboutMe());
        user.setInterests(source.getInterests());
        return user;
    }


    public static List<UserInterest> toListUserInterest() {
        List<Interest> interest = new ArrayList<>(Interest.getRandomInterest());
        List<UserInterest> userInterest = new ArrayList<>();
        interest.forEach(interest1 -> userInterest.add(new UserInterest(interest1)));
        return userInterest;
    }
}