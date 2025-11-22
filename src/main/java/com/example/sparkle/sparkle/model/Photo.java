package com.example.sparkle.sparkle.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "photos")
@Getter
@Setter
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String url;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type")
    private String fileType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo photo)) return false;
        return Objects.equals(getId(), photo.getId()) && Objects.equals(getFileName(), photo.getFileName()) && Objects.equals(getUrl(), photo.getUrl()) && Objects.equals(getFileSize(), photo.getFileSize()) && Objects.equals(getFileType(), photo.getFileType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFileName(), getUrl(), getFileSize(), getFileType());
    }
}

