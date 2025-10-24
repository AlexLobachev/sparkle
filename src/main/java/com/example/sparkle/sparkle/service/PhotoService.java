package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.Photo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PhotoService {
    /**
     * Загрузка фотографии пользователя
     */

    Photo uploadUserPhoto(MultipartFile multipartFile, Long userId) throws IOException;

    /**
     * Удаление фотографии пользователя
     */

    void removeUserPhoto(Photo userPhoto) throws IOException;



}