package com.example.sparkle.sparkle.model;

import com.example.sparkle.sparkle.dto.user.UserInterestsDto;
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
     * Преобразует список интересов в DTO
     */
    public static UserInterestsDto toUserInterestDto(List<UserInterest> userInterest) {
        if (userInterest == null || userInterest.isEmpty()) {
            throw new IllegalArgumentException("Список интересов не может быть пустым");
        }
        UserInterestsDto dto = new UserInterestsDto();
        User first = userInterest.get(0).getUser();
        dto.setUserId(first.getId());
        dto.setInterests(userInterest.stream().map(UserInterest::getInterest).toList());
        return dto;
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
}