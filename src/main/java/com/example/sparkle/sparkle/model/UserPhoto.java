package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "user_photo")
public class UserPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "photo_id", nullable = false)
    private Photo photo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPhoto userPhoto)) return false;
        return Objects.equals(getId(), userPhoto.getId()) && Objects.equals(getUser(), userPhoto.getUser()) && Objects.equals(getPhoto(), userPhoto.getPhoto());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser(), getPhoto());
    }
}
