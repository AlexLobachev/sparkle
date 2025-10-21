package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.Photo;
import com.example.sparkle.sparkle.model.UserPhoto;

import java.util.List;

public interface UserPhotoService {
    /**
     * Сохранение фото пользователя
     */
    UserPhoto saveUserPhoto(UserPhoto userPhoto);


    /**
     * Удаление фото пользователя
     */
    void deleteByPhotoId(Long photoId);
    /**
     * Получение фото пользователя
     */

    public UserPhoto getPhotoById(Long photoId);

    /**
     * Получение всех фото пользователя
     */
    public List<UserPhoto> getAllPhotoByIdUser(Long userId);
}
