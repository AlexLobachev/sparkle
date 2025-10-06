package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.user.UserDTO;
import com.example.sparkle.sparkle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> registerUser(UserDTO userDto) {
        return null;
    }

    /**
     * Аутентификация пользователя
     */

    public ResponseEntity<?> authenticateUser(UserDTO userDto) {
        return null;
    }

    /**
     * Получение информации о текущем авторизованном пользователе
     */

    public ResponseEntity<UserDTO> getCurrentUserProfile() {
        return null;
    }

    /**
     * Редактирование профиля пользователя
     */

    public ResponseEntity<UserDTO> updateUserProfile(UserDTO updatedUserDto) {
        return null;
    }

    /**
     * Загрузка фотографии пользователя
     */

    public ResponseEntity<String> uploadUserPhoto(MultipartFile file) {
        return null;
    }

    /**
     * Удаление фотографии пользователя
     */

    public ResponseEntity<Void> removeUserPhoto(Long photoId) {
        return null;
    }
}
