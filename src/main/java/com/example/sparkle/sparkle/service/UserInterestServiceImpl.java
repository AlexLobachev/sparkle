package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserInterest;
import com.example.sparkle.sparkle.repository.UserInterestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserInterestServiceImpl implements UserInterestService {
    private final UserInterestRepository userInterestRepository;
    private final UserService userService;

    @Autowired
    public UserInterestServiceImpl(UserInterestRepository userInterestRepository, UserService userService) {
        this.userInterestRepository = userInterestRepository;
        this.userService = userService;
    }

    /**
     * Сохраняем интересы пользователю
     */
    @Override
    public List<UserInterest> saveAllInterest(List<UserInterest> listInterest) {
        log.info("Сохранение интереса");

        return userInterestRepository.saveAll(listInterest);
    }

    @Override
    public UserInterest saveInterest(UserInterest interest) {
        log.info("Сохранение интереса");
        return userInterestRepository.save(interest);
    }

    /**
     * Получаем все интересы пользователя по его ID
     */
    @Override
    public List<UserInterest> getAllInterestUserById(Long userId) {
        return userInterestRepository.findAllByUserId(userId);
    }

    /**
     * Получаем интерес по ID пользователя
     * Метод нужен что-бы не дублировать интерес если он уже есть в БД
     */
    @Override
    public UserInterest getInterestUserById(UserInterest userInterest) {
        return userInterestRepository.findAllByUserIdAndInterest(userInterest.getUser().getId(), userInterest.getInterest());

    }

    /**
     * Получаем все пользователей с общими интересами
     * Если параметр ID не указан возвращаются пользователи с общими интересами
     * Если параметр ID указан то возвращаются пользователи только с теми интересами которые есть у заданного пользователя
     */
    @Override
    public List<User> getUsersWithTheSameInterests(Long userId) {
        List<String> interests = new ArrayList<>();

            getAllInterestUserById(userId)
                    .forEach(inter -> interests.add(inter.getInterest().name()));
            return userInterestRepository.getUsersWithTheSameInterestsByUserId(interests);
    }

    @Override
    public List<User> getUsersWithTheSameInterests() {

        return userInterestRepository.getUsersWithTheSameInterests();
    }

    /**
     * Удаляем интерес у пользователя
     */
    @Override
    @Transactional
    public void deleteInterestByUserId(UserInterest userInterest) {

        userInterestRepository.deleteByUserIdAndInterest(userInterest.getUser().getId(), userInterest.getInterest());
        //userInterestRepository.deleteById(userInterest.getId());
    }

}
