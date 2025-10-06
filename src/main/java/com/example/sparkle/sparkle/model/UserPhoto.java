package com.example.sparkle.sparkle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "photos")
@Getter
@Setter
public class UserPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPhoto userPhoto)) return false;
        return getId().equals(userPhoto.getId()) && getFilePath().equals(userPhoto.getFilePath()) && getUser().equals(userPhoto.getUser());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFilePath(), getUser());
    }
}