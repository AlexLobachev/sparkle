package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.user.*;
import com.example.sparkle.sparkle.model.Gender;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Регистрация нового пользователя
     * Пользователь вводит Имя, Пол, Дату рождения
     */
    public UserDtoRegister registerUser(UserDtoRegister userDtoRegister) {
        User user = new User();
        user.setUsername(userDtoRegister.getUsername());
        user.setGender(userDtoRegister.getGender());
        user.setBirthDate(userDtoRegister.getBirthDate());
        user.setPassword(user.getPassword());
        user.setEmail(userDtoRegister.getEmail());
        userRepository.save(user);
        return userDtoRegister;
    }

    /**
     * Аутентификация пользователя
     */

    public UserDtoGetAuthenticateUser authenticateUser(UserDtoAuthenticate userDtoAuthenticate) {
        User user = userRepository.findByEmail(userDtoAuthenticate.getEmail());
        return userDtoGetAuthenticateUser(user);
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

    public UserDtoGetAuthenticateUser updateUserProfile(UserDtoRegister userDtoRegister) {
        Gender gender = userDtoRegister.getGender();
        String strGender = gender.toString(gender);
        User user = userRepository.userUpdate(
                userDtoRegister.getUsername(), userDtoRegister.getPassword(),
                strGender, userDtoRegister.getEmail(),
                userDtoRegister.getBirthDate(), userDtoRegister.getId()).get();
        return userDtoGetAuthenticateUser(user);

    }


    /**
     * Получение профиля пользователя по email
     */

    public User getUserByEmail(String email) {
        return geUserDtoCheck(userRepository.findByEmail(email));
    }

    /**
     * Получение профиля пользователя по id
     */
    public UserDtoAuthenticate getUserById(Long userId) {
        return geUserAuthenticate(userRepository.findById(userId).get());
    }

    /**
     * Получение списка всех пользователей
     */
    public List<UserDtoAuthenticate> getUserAll() {
        List<UserDtoAuthenticate> userDtoAuthenticate = new ArrayList<>();
        userRepository.findAll().stream().filter(user -> userDtoAuthenticate.add(geUserAuthenticate(user)));
        return userDtoAuthenticate;
    }

    private UserDtoAuthenticate geUserAuthenticate(User user) {
        UserDtoAuthenticate userDtoAuthenticate = new UserDtoAuthenticate();
        userDtoAuthenticate.setPassword(user.getPassword());
        userDtoAuthenticate.setEmail(user.getEmail());
        return userDtoAuthenticate;
    }

    private User geUserDtoCheck(User user) {
        User userDtoAuthenticate = new User();
        userDtoAuthenticate.setPassword(user.getPassword());
        userDtoAuthenticate.setEmail(user.getEmail());
        return userDtoAuthenticate;
    }

    private UserDtoGetAuthenticateUser userDtoGetAuthenticateUser(User user) {
        UserDtoGetAuthenticateUser userDtoGetAuthenticateUser = new UserDtoGetAuthenticateUser();
        userDtoGetAuthenticateUser.setUsername(user.getUsername());
        userDtoGetAuthenticateUser.setGender(user.getGender());
        userDtoGetAuthenticateUser.setEmail(user.getEmail());
        userDtoGetAuthenticateUser.setBirthDate(user.getBirthDate());
        return userDtoGetAuthenticateUser;
    }
}
