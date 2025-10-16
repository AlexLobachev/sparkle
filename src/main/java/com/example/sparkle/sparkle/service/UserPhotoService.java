package com.example.sparkle.sparkle.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserPhotoService {
    /**
     * Загрузка фотографии пользователя
     */

    MultipartFile uploadUserPhoto(MultipartFile multipartFile, Long userId) throws IOException;

    /**
     * Удаление фотографии пользователя
     */

    void removeUserPhoto(Long photoId);
}
