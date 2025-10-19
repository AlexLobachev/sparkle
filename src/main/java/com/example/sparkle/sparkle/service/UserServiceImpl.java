package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.user.UserDtoGet;
import com.example.sparkle.sparkle.dto.user.UserDtoRegister;
import com.example.sparkle.sparkle.dto.user.UserDtoRegisterGet;
import com.example.sparkle.sparkle.dto.user.UserDtoUpdate;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    public UserDtoRegisterGet registerUser(UserDtoRegister userDtoRegister) {
        User user = userRepository.save(userBuilder(userDtoRegister));
        return userDtoRegisterGetBuilder(user);
    }

    /**
     * Аутентификация пользователя
     */

    public UserDtoRegisterGet authenticateUser(User userDtoAuthenticate) {
        User user = userRepository.findByEmail(userDtoAuthenticate.getEmail());
        return userDtoRegisterGetBuilder(user);
    }

    /**
     * Получение информации о текущем авторизованном пользователе
     */

    public UserDtoGet getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId).get();
        UserDtoGet userDtoGet = new UserDtoGet();
        userDtoGet.setUsername(user.getUsername());
        userDtoGet.setGender(user.getGender());
        userDtoGet.setCity(user.getCity());
        userDtoGet.setBirthDate(user.getBirthDate());
        userDtoGet.setEmail(user.getEmail());
        userDtoGet.setInterests(user.getInterests());
        userDtoGet.setPhotos(user.getPhotos());
        return userDtoGet;
    }

    /**
     * Редактирование профиля пользователя
     */
    @Transactional
    public int updateUserProfile(UserDtoUpdate userDtoUpdate) {
        int affectedRows =
                userRepository.userUpdate(
                        userDtoUpdate.getUsername(), userDtoUpdate.getPassword(),
                        userDtoUpdate.getGender(), userDtoUpdate.getEmail(),
                        userDtoUpdate.getBirthDate(), userDtoUpdate.getId());
        entityManager.refresh(userRepository.findById(userDtoUpdate.getId()).get());
        return affectedRows;
    }


    /**
     * Получение профиля пользователя по email
     */

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Получение профиля пользователя по id
     */
    public UserDtoRegisterGet getUserById(Long userId) {
        return userDtoRegisterGetBuilder(userRepository.findById(userId).get());
    }

    /**
     * Получение списка всех пользователей
     */
    public List<UserDtoRegisterGet> getUserAll() {

        return userRepository.findAll().stream().map(this::userDtoRegisterGetBuilder).collect(Collectors.toList());
    }

    /**
     * Удаление пользователя по ID
     */
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    private UserDtoRegisterGet userDtoRegisterGetBuilder(User user) {
        UserDtoRegisterGet userDtoRegisterGet = new UserDtoRegisterGet();
        userDtoRegisterGet.setId(user.getId());
        userDtoRegisterGet.setUsername(user.getUsername());
        userDtoRegisterGet.setGender(user.getGender());
        userDtoRegisterGet.setEmail(user.getEmail());
        userDtoRegisterGet.setBirthDate(user.getBirthDate());
        return userDtoRegisterGet;
    }

    private User userBuilder(UserDtoRegister userDtoRegister) {

        User user = new User();
        user.setUsername(userDtoRegister.getUsername());
        user.setGender(userDtoRegister.getGender());
        user.setBirthDate(userDtoRegister.getBirthDate());
        user.setPassword(userDtoRegister.getPassword());
        user.setEmail(userDtoRegister.getEmail());
        return user;
    }
}
