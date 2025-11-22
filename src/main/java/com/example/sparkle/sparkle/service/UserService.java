package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.LocationRequestDto;
import com.example.sparkle.sparkle.dto.user.UserDto;
import com.example.sparkle.sparkle.dto.user.UserDtoUpdate;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с пользователями
 */
public interface UserService {
    /**
     * Регистрация нового пользователя (вручную через Email) (Не поддерживается)
     */
    Optional<User> registerUser(User user);

    /**
     * Регистрация нового пользователя через соц. сеть
     */

    Optional<User> registerUserBySocialNetwork(User user);


    /**
     * Редактирование профиля пользователя
     */

    Optional<UserDtoUpdate> updateUserProfile(Long userId, UserDtoUpdate userDtoUpdate);

    /**
     * Ввод параметров при регистрации пользователя (интересы, пол и пр.)
     */
    Optional<User> setupUserProfile(UserDtoUpdate userDtoUpdate);

    /**
     * Получение профиля пользователя по id
     */
    Optional<UserDto> getUserById(Long userId);

    /**
     * Получение списка всех пользователей
     */
    List<UserDto> getUserAll();

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

    /**
     * Получение пользователя по ID провайдера
     */
    Optional<User> getUserByExternalId(String externalId);

    /**
     * Получение пользователя по ID и провайдеру
     */

    Optional<User> findByExternalIdAndProvider(String externalId, String provider);

}
