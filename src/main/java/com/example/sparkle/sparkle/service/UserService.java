package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.user.UserDtoGet;
import com.example.sparkle.sparkle.dto.user.UserDtoRegister;
import com.example.sparkle.sparkle.dto.user.UserDtoRegisterGet;
import com.example.sparkle.sparkle.dto.user.UserDtoUpdate;
import com.example.sparkle.sparkle.model.User;

import java.util.List;

public interface UserService {

    UserDtoRegisterGet registerUser(UserDtoRegister userDtoRegister);

    /**
     * Аутентификация пользователя
     */

    UserDtoRegisterGet authenticateUser(User userDtoAuthenticate);

    /**
     * Получение информации о текущем авторизованном пользователе
     */

    UserDtoGet getCurrentUserProfile(Long userId);

    /**
     * Редактирование профиля пользователя
     */

    int updateUserProfile(UserDtoUpdate userDtoRegister);
    /**
     * Получение профиля пользователя по email
     */
    User getUserByEmail(String email);
    /**
     * Получение профиля пользователя по id
     */
    UserDtoRegisterGet getUserById(Long userId);
    /**
     * Получение списка всех пользователей
     */
    List<UserDtoRegisterGet> getUserAll();

    /**
     * Удаление пользователя по ID
     */
    void deleteUserById(Long userId);
}
