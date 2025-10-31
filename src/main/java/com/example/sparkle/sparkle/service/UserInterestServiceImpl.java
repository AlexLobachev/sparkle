package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserInterest;
import com.example.sparkle.sparkle.repository.UserInterestRepository;
import com.example.sparkle.sparkle.validator.ValidatorInterest;
import com.example.sparkle.sparkle.validator.ValidatorUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * Класс для работы с интересами пользователя
 */
@Service
@Slf4j
public class UserInterestServiceImpl implements UserInterestService {
    private final UserInterestRepository userInterestRepository;
    private final UserService userService;
    private final ValidatorInterest validatorInterest;
    private final ValidatorUser validatorUser;

    @Autowired
    public UserInterestServiceImpl(UserInterestRepository userInterestRepository, UserService userService,
                                   ValidatorInterest validatorInterest, ValidatorUser validatorUser) {
        this.userInterestRepository = userInterestRepository;
        this.userService = userService;
        this.validatorInterest = validatorInterest;
        this.validatorUser = validatorUser;
    }

    /**
     * Сохраняем интересы пользователю списком
     */
    @Override
    public List<UserInterest> saveAllInterest(Long userId, List<UserInterest> listInterest) {
        User user = userService.getUserById(userId).orElseThrow();
        // Получаем текущие интересы пользователя
        List<UserInterest> existingInterests = userInterestRepository.findAllByUserId(userId);
        Set<Interest> existingInterestSet = existingInterests.stream()
                .map(UserInterest::getInterest)
                .collect(Collectors.toSet());
        // Фильтруем новые интересы, оставляя только уникальные
        List<UserInterest> newInterests = listInterest.stream()
                .filter(interest -> !existingInterestSet.contains(interest.getInterest()))
                .peek(interest -> {
                    interest.setUser(user);
                    interest.setId(null); // сбрасываем ID для новой записи
                }).toList();
        // Возвращаем обновленный список интересов

        if (!newInterests.isEmpty()) {
            userInterestRepository.saveAll(newInterests);
        }

        List<UserInterest> updatedInterests = getAllInterestUserById(user.getId());
        validatorInterest.interestNoContent(updatedInterests);
        return updatedInterests;
    }
    /**
     * Сохраняем интересы пользователю по одному
     */
    @Override
    public UserInterest saveInterest(Long userId, UserInterest interest) {
        User user = userService.getUserById(userId).orElseThrow();
        interest.setUser(user);
        if (userInterestRepository.findAllByUserId((user.getId()))
                .stream()
                .anyMatch(userInterest -> userInterest.getInterest().equals(interest.getInterest()))) {
            interest.setUser(user);
            return getAllByUserIdAndInterest(interest);
        }

        return userInterestRepository.save(interest);
    }

    /**
     * Получаем все интересы пользователя по его ID
     */
    @Override
    public List<UserInterest> getAllInterestUserById(Long userId) {
        userService.getUserById(userId);
        List<UserInterest> userInterests = userInterestRepository.findAllByUserId(userId);
        validatorInterest.interestNoContent(userInterests);
        return userInterests;
    }

    /**
     * Получаем интерес по ID пользователя
     * Метод нужен что-бы не дублировать интерес если он уже есть в БД
     */
    @Override
    public UserInterest getAllByUserIdAndInterest(UserInterest userInterest) {
        return userInterestRepository.findAllByUserIdAndInterest(userInterest.getUser().getId(),userInterest.getInterest());

    }

    /**
     * Получаем пользователей с общими интересами как у пользователя по ID

     */
    @Override
    public List<User> getUsersWithTheSameInterests(Long userId) {
        userService.getUserById(userId);
        List<String> interests = new ArrayList<>();
        getAllInterestUserById(userId)
                .forEach(inter -> interests.add(inter.getInterest().name()));
        return userInterestRepository.getUsersWithTheSameInterestsByUserId(interests);
    }
    /**
     * Получаем всех пользователей с общими интересами

     */
    @Override
    public List<User> getAllUsersWithTheSameInterests() {
        List<User> usersInterest = userInterestRepository.getUsersWithTheSameInterests();
        validatorUser.userNoContent(usersInterest);
        return usersInterest;
    }

    /**
     * Удаляем интерес у пользователя
     */
    @Override
    @Transactional
    public void deleteInterestByUserId(Long userId, UserInterest interest) {
        User user = userService.getUserById(userId).orElseThrow();
        interest.setUser(user);
        userInterestRepository.deleteByUserIdAndInterest(userId, interest.getInterest());
        validatorInterest.interestConflictDelete(getAllByUserIdAndInterest(interest));
        userInterestRepository.deleteByUserIdAndInterest(interest.getUser().getId(), interest.getInterest());
    }

}
