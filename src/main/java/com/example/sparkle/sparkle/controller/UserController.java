package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.user.UserDtoAuthenticate;
import com.example.sparkle.sparkle.dto.user.UserDtoGet;
import com.example.sparkle.sparkle.dto.user.UserDtoRegister;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;


    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;

    }

    /**
     * Регистрация нового пользователя
     * Пользователь вводит Имя, Пол, Дату рождения
     */
    @PostMapping("/register/next-page")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDtoRegister userDtoRegister) {
        User user = userService.getUserByEmail(userDtoRegister.getEmail());
        if (user.getEmail().equals(userDtoRegister.getEmail())) {
            log.debug("Попытка зарегистрироваться по существующему email = {}", userDtoRegister.getEmail());
            return new ResponseEntity<>("Пользователь с таким email уже зарегистрирован", HttpStatus.CONFLICT);
        }
        log.info("Успешная регистрация пользователя");
        return ResponseEntity.ok(userService.registerUser(userDtoRegister));
    }

    /**
     * Аутентификация пользователя
     * Если пользователь уже зарегистрировался и хочет войти под своим email и паролем
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserDtoAuthenticate userDtoAuthenticate) {
        User user = userService.getUserByEmail(userDtoAuthenticate.getEmail());
        if (user == null) {
            log.debug("Пользователь с email = {} не найден", userDtoAuthenticate.getEmail());
            return new ResponseEntity<>("Пользователь не найден", HttpStatus.NOT_FOUND);
        }
        if (!user.getPassword().equals(userDtoAuthenticate.getPassword())) {
            log.debug("Пароль введен не верно");
            return new ResponseEntity<>("Пароль введен не верно", HttpStatus.BAD_REQUEST);
        }
        log.info("Успешный вход пользователя");
        return ResponseEntity.ok(userService.authenticateUser(userDtoAuthenticate));
    }


    /**
     * Получение информации о текущем авторизованном пользователе
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getCurrentUserProfile(@PathVariable Long userId) {
        UserDtoGet userDtoGet = userService.getCurrentUserProfile(userId);
        if (userDtoGet == null) {
            log.debug("Пользователь с ID = {} не найден", userId);
            new ResponseEntity<>("Пользователь не найден", HttpStatus.NOT_FOUND);
        }
        log.info("Пользователь с ID = {} получен", userId);
        return ResponseEntity.ok(userDtoGet);
    }

    /**
     * Редактирование профиля пользователя
     */
    @PatchMapping("/update-profile/{userId}")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long userId, @Valid @RequestBody UserDtoRegister userDtoRegister) {
        UserDtoAuthenticate user = userService.getUserById(userId);
        if (user == null) {
            log.debug("Пользователь с email = {} не найден", userDtoRegister.getEmail());
            return new ResponseEntity<>("Пользователь не найден", HttpStatus.NOT_FOUND);
        }
        if (!user.getPassword().equals(userDtoRegister.getOldPassword())) {
            log.debug("Старый пароль не совпадает");
            return new ResponseEntity<>("Старый пароль не совпадает", HttpStatus.BAD_REQUEST);
        }
        if (!user.getEmail().equals(userDtoRegister.getEmail())) {
            log.debug("Пользователь с email = {} уже существует", user.getEmail());
            return new ResponseEntity<>("Пользователь с таким email уже зарегистрирован", HttpStatus.CONFLICT);
        }
        userDtoRegister.setId(userId);
        log.info("Успешное обновление пользователя");

        return ResponseEntity.ok(userService.updateUserProfile(userDtoRegister));
    }

    @GetMapping
    public ResponseEntity<?> getUserAll() {
        List<UserDtoAuthenticate> users = userService.getUserAll();
        if (userService.getUserAll().isEmpty()) {
            log.info("Пользователи отсутствуют");
            new ResponseEntity<>("Пользователи отсутствуют", HttpStatus.NO_CONTENT);
        }
        return (ResponseEntity<?>) userService.getUserAll();
    }


}