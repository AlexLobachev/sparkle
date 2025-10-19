package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.user.*;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.repository.UserRepository;
import com.example.sparkle.sparkle.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/sparkle/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;

    }

    /**
     * Регистрация нового пользователя
     * Пользователь вводит Имя, Пол, Дату рождения
     */
    @PostMapping("/register/next-page")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDtoRegister userDtoRegister) {
        User user = userService.getUserByEmail(userDtoRegister.getEmail());
        try {
            if (user != null && user.getEmail().equals(userDtoRegister.getEmail())) {
                log.debug("Попытка зарегистрироваться по существующему email = {}", userDtoRegister.getEmail());
                return ResponseEntity.badRequest().body("Пользователь с таким email уже зарегистрирован");
            }
            return ResponseEntity.ok(userService.registerUser(userDtoRegister));
        } catch (MethodArgumentNotValidException e) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : e.getBindingResult().getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }
    }

    /**
     * Аутентификация пользователя
     * Если пользователь уже зарегистрировался и хочет войти под своим email и паролем
     */
    @GetMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserDtoAuthenticate userDtoAuthenticate) {
        User user = userService.getUserByEmail(userDtoAuthenticate.getEmail());
        if (user == null) {
            log.debug("Пользователь с email = {} не найден", userDtoAuthenticate.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }
        if (!user.getPassword().equals(userDtoAuthenticate.getPassword())) {
            log.debug("Пароль введен не верно");
            return new ResponseEntity<>("Пароль введен не верно", HttpStatus.BAD_REQUEST);
        }
        log.info("Успешный вход пользователя");
        return ResponseEntity.ok(userService.authenticateUser(user));
    }


    /**
     * Получение информации о текущем авторизованном пользователе
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getCurrentUserProfile(@PathVariable Long userId) {
        UserDtoGet userDtoGet = userService.getCurrentUserProfile(userId);
        if (userDtoGet != null) {
            log.info("Пользователь с ID = {} получен", userId);
            return ResponseEntity.ok(userDtoGet);

        }
        log.debug("Пользователь с ID = {} не найден", userId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
    }

    /**
     * Редактирование профиля пользователя
     */
    @PatchMapping("/update-profile/{userId}")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long userId,
                                               @Valid @RequestBody UserDtoUpdate userDtoUpdate) {
        try {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }
            userDtoUpdate.setId(userId);
            UserDtoRegister validationDto = new UserDtoRegister();
            validationDto.setUsername(optionalUser.get().getUsername());
            validationDto.setEmail(optionalUser.get().getEmail());
            validationDto.setPassword(optionalUser.get().getPassword());
            validationDto.setBirthDate(optionalUser.get().getBirthDate());
            validationDto.setGender(optionalUser.get().getGender());
            if (userDtoUpdate.getUsername() != null) {
                validationDto.setUsername(userDtoUpdate.getUsername());
            }
            if (userDtoUpdate.getEmail() != null) {
                validationDto.setEmail(userDtoUpdate.getEmail());
            }
            if (userDtoUpdate.getPassword() != null) {
                validationDto.setPassword(userDtoUpdate.getPassword());
            }
            if (userDtoUpdate.getBirthDate() != null) {
                validationDto.setBirthDate(userDtoUpdate.getBirthDate());
            }
            if (userDtoUpdate.getGender() != null) {
                validationDto.setGender(userDtoUpdate.getGender());
            }
            log.debug("Валидация данных");
            log.debug(">>>>>> " + validationDto);
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

            if (userDtoUpdate.getUsername() == null || userDtoUpdate.getUsername().isBlank()) {
                Set<ConstraintViolation<UserDtoRegister>> violations = validator.validateProperty(
                        validationDto, "username"
                );
                if (!violations.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(violations.iterator().next().getMessage())
                            ;
                }
            }

            if (userDtoUpdate.getEmail() != null || userDtoUpdate.getEmail() == null) {
                Set<ConstraintViolation<UserDtoRegister>> violations = validator.validateProperty(
                        validationDto, "email"
                );
                if (!violations.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(violations.iterator().next().getMessage())
                            ;
                }
            }

            if (userDtoUpdate.getPassword() != null || userDtoUpdate.getPassword() == null) {
                Set<ConstraintViolation<UserDtoRegister>> violations = validator.validateProperty(
                        validationDto, "password"
                );
                if (!violations.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(violations.iterator().next().getMessage())
                            ;
                }
            }
            if (userDtoUpdate.getGender() != null || userDtoUpdate.getGender() == null) {
                Set<ConstraintViolation<UserDtoRegister>> violations = validator.validateProperty(
                        validationDto, "gender"
                );
                if (!violations.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(violations.iterator().next().getMessage())
                            ;
                }
            }
            if (userDtoUpdate.getBirthDate() != null || userDtoUpdate.getBirthDate() == null) {
                Set<ConstraintViolation<UserDtoRegister>> violations = validator.validateProperty(
                        validationDto, "birthDate"
                );
                if (!violations.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(violations.iterator().next().getMessage())
                            ;
                }
            }

            int affectedRows = userService.updateUserProfile(userDtoUpdate);

            if (affectedRows == 0) {
                log.warn("Обновление пользователя с ID = {} не выполнено - запись не найдена", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }
            log.debug("Обновление пользователя с ID = {} прошло успешно", userDtoUpdate.getId());

            return ResponseEntity.ok(userService.getUserById(userId));


        } catch (RuntimeException e) {
            if (e.getMessage().contains("unique")) {
                log.debug("Пользователь с email = {} уже зарегистрирован {} ", userDtoUpdate.getEmail(), e.getMessage());
                return new ResponseEntity<>("Пользователь с таким email уже зарегистрирован", HttpStatus.BAD_REQUEST);
            } else {
                log.debug(e.getMessage());
                log.debug("Не удалось обновить пользователя с ID = {}", userDtoUpdate.getId());
                return ResponseEntity.badRequest().build();
            }
        }

    }

    /**
     * Получение всех пользователей
     */
    @GetMapping
    public ResponseEntity<?> getUserAll() {
        List<UserDtoRegisterGet> users = userService.getUserAll();
        if (users.isEmpty()) {
            log.info("Пользователи отсутствуют");
            return ResponseEntity.noContent().build();

        }
        return ResponseEntity.ok(users);
    }
    /**
     * Получение пользователя по ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        UserDtoRegisterGet userDtoRegisterGet = userService.getUserById(userId);
        if (userDtoRegisterGet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }
        return ResponseEntity.ok(userDtoRegisterGet);
    }

    /**
     * Удаление пользователя по ID
     */
    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        UserDtoRegisterGet userDtoRegisterGet = userService.getUserById(userId);
        if (userDtoRegisterGet == null) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }
        userService.deleteUserById(userId);
    }

}