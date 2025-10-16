package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.user.*;
import com.example.sparkle.sparkle.model.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {

    UserDtoRegister registerUser(UserDtoRegister userDtoRegister);

    /**
     * Аутентификация пользователя
     */

    UserDtoGetAuthenticateUser authenticateUser(UserDtoAuthenticate userDtoAuthenticate);

    /**
     * Получение информации о текущем авторизованном пользователе
     */

    UserDtoGet getCurrentUserProfile(Long userId);

    /**
     * Редактирование профиля пользователя
     */

    UserDtoGetAuthenticateUser updateUserProfile(UserDtoRegister userDtoRegister);
    /**
     * Получение профиля пользователя по email
     */
    User getUserByEmail(String email);
    /**
     * Получение профиля пользователя по id
     */
    UserDtoAuthenticate getUserById(Long userId);

    List<UserDtoAuthenticate> getUserAll();
}
