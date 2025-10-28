package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.repository.UserRepository;
import com.example.sparkle.sparkle.validator.ValidatorUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * Регистрация нового пользователя
     * Пользователь вводит Имя, Пол, Дату рождения
     */
    @Override
    public User registerUser(User user) {

        return userRepository.save(user);
    }

    /**
     * Получение информации о текущем авторизованном пользователе
     */
    @Override
    public Optional<User> getCurrentUserProfile(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        ValidatorUser.userNotFoundAndForbidden(user.orElseThrow(), userId);
        return userRepository.findById(userId);
    }

    /**
     * Редактирование профиля пользователя
     */

    @Transactional
    @Override
    public int updateUserProfile(User user) {
        int affectedRows =
                userRepository.userUpdate(
                        user.getUsername(),
                        user.getGender().toString(),
                        user.getPreferredGender().toString(),
                        user.getEmail(),
                        user.getBirthDate(),
                        user.getAboutMe(),
                        user.getId());
        entityManager.refresh(userRepository.findById(user.getId()).orElseThrow());
        return affectedRows;
    }


    /**
     * Получение профиля пользователя по email
     */
    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Получение профиля пользователя по id
     */
    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Получение списка всех пользователей
     */
    @Override
    public List<User> getUserAll() {
        Sort sort = Sort.by("id").ascending();
        return userRepository.findAll(sort);
    }

    /**
     * Удаление пользователя по ID
     */
    @Override
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Получение пользователя по имени
     */
    @Override
    public User getUserByUserName(String name) {
        return userRepository.findByUsername(name).orElseThrow();
    }

}
