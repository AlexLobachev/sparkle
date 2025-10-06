package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.user.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    ResponseEntity<?> registerUser(UserDTO userDto);

    /**
     * Аутентификация пользователя
     */

    ResponseEntity<?> authenticateUser(UserDTO userDto);

    /**
     * Получение информации о текущем авторизованном пользователе
     */

    ResponseEntity<UserDTO> getCurrentUserProfile();

    /**
     * Редактирование профиля пользователя
     */

    ResponseEntity<UserDTO> updateUserProfile(UserDTO updatedUserDto);

    /**
     * Загрузка фотографии пользователя
     */

    ResponseEntity<String> uploadUserPhoto(MultipartFile file);

    /**
     * Удаление фотографии пользователя
     */

    ResponseEntity<Void> removeUserPhoto(Long photoId);
}
