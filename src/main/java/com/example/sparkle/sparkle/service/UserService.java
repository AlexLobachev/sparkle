package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    /**
     * Регистрация пользователя
     */
    User registerUser(User user);


    /**
     * Получение информации о текущем авторизованном пользователе
     */

    Optional<User> getCurrentUserProfile(Long userId);

    /**
     * Редактирование профиля пользователя
     */

    int updateUserProfile(User user);
    /**
     * Получение профиля пользователя по email
     */
    Optional<User> getUserByEmail(String email);
    /**
     * Получение профиля пользователя по id
     */
    Optional<User> getUserById(Long userId);
    /**
     * Получение списка всех пользователей
     */
    List<User> getUserAll();

    /**
     * Удаление пользователя по ID
     */
    void deleteUserById(Long userId);

}
