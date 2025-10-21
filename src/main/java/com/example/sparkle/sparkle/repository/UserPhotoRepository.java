package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPhotoRepository extends JpaRepository <UserPhoto,Long> {

    List<UserPhoto> findByUserId(Long userId);
    UserPhoto findByPhotoId(Long photoId);
    void deleteByPhotoId(Long photoId);



}
