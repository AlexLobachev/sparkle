package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.LocationRequestDto;
import com.example.sparkle.sparkle.dto.user.UserDtoUpdate;
import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
/**
 * Интерфейс для работы с пользователями
 */
public interface UserService {
    /**
     * Регистрация пользователя
     */
    Optional<User> registerUser(User user);


    /**
     * Редактирование профиля пользователя
     */

    Optional<User> updateUserProfile(Long userId, UserDtoUpdate userDtoUpdate);

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
    /**
     * Получение пользователя по имени
     */
    Optional<User> getUserByUserName(String name);
    /**
     * Сохранение локации пользователя
     */
    Optional<User> saveUserLocation(LocationRequestDto location, Long userId);

}
