package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserInterest;

import java.util.List;
/**
 * Интерфейс для работы с интересами пользователя
 */
public interface UserInterestService {
    /**
     * Сохраняем интересы пользователю списком
     */
    List<UserInterest> saveAllInterest(Long userId, List<UserInterest> listInterest);
    /**
     * Сохраняем интересы пользователю по одному
     */
    UserInterest saveInterest(Long userId, UserInterest interest);

    /**
     * Получаем все интересы пользователя по его ID
     */
    List<UserInterest>getAllInterestUserById(Long userId);
    /**
     * Получаем интерес по ID пользователя
     * Метод нужен что-бы не дублировать интерес если он уже есть в БД
     */
    UserInterest getAllByUserIdAndInterest(UserInterest userInterest);
    /**
     * Получаем пользователей с общими интересами как у пользователя по ID

     */
    List<User> getUsersWithTheSameInterests(Long userId);
    /**
     * Получаем всех пользователей с общими интересами

     */
    List<User> getAllUsersWithTheSameInterests();
    /**
     * Удаляем интерес у пользователя
     */
    void deleteInterestByUserId(Long userId,UserInterest interest);
}
