package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserInterest;

import java.util.List;

public interface UserInterestService {
    /**
     * Сохраняем интересы пользователю
     */
    List<UserInterest> saveAllInterest(List<UserInterest> listInterest);
    UserInterest saveInterest(UserInterest interest);

    /**
     * Получаем все интересы пользователя по его ID
     */
    List<UserInterest>getAllInterestUserById(Long userId);
    UserInterest getInterestUserById(UserInterest userInterest);
    List<User> getUsersWithTheSameInterests(Long userId);
    List<User> getUsersWithTheSameInterests();
    void deleteInterestByUserId(UserInterest userInterest);
}
