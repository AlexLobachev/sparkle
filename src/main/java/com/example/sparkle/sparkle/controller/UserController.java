package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.builder.BuilderUser;
import com.example.sparkle.sparkle.dto.user.UserDtoRegister;
import com.example.sparkle.sparkle.dto.user.UserDtoUpdate;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.service.UserService;
import com.example.sparkle.sparkle.validator.ValidatorUser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
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
        Optional<User> user = userService.getUserByEmail(userDtoRegister.getEmail());
        try {
            if (user.isPresent() && user.get().getEmail().equals(userDtoRegister.getEmail())) {
                log.info("Попытка зарегистрироваться по существующему email = {}", userDtoRegister.getEmail());
                return ResponseEntity.badRequest().body("Пользователь с таким email уже зарегистрирован");
            }
            user = Optional.ofNullable(userService.registerUser(BuilderUser.userBuilder(userDtoRegister)));
            return ResponseEntity.ok(BuilderUser.userDtoRegisterBuilder(user.orElseThrow()));
        } catch (MethodArgumentNotValidException e) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : e.getBindingResult().getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }
    }


    /**
     * Получение информации о текущем авторизованном пользователе
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getCurrentUserProfile(@PathVariable @Min(1) Long userId) {
        Optional<User> user = userService.getCurrentUserProfile(userId);
        ResponseEntity<?> validationResult = ValidatorUser.userNotFoundAndForbidden(
                user.orElse(null),
                userId);

        if (!validationResult.getStatusCode().is2xxSuccessful()) {
            return validationResult;
        }
        log.info("Пользователь с ID = {} получен", userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Редактирование профиля пользователя
     */
    @PatchMapping("/update-profile/{userId}")
    public ResponseEntity<?> updateUserProfile(@PathVariable @Min(1) Long userId,
                                               @Valid @RequestBody UserDtoUpdate userDtoUpdate) {
        try {
            Optional<User> user = userService.getUserById(userId);
            ResponseEntity<?> validationResult = ValidatorUser.userNotFoundAndForbidden(
                    user.orElse(null),
                    userId);

            if (!validationResult.getStatusCode().is2xxSuccessful()) {
                return validationResult;
            }

            UserDtoRegister validationDto = BuilderUser.userDtoRegisterBuilder(user.orElseThrow());
            if (userDtoUpdate.getUsername() != null) {
                validationDto.setUsername(userDtoUpdate.getUsername());
            }
            if (userDtoUpdate.getEmail() != null) {
                validationDto.setEmail(userDtoUpdate.getEmail());
            }
            if (userDtoUpdate.getBirthDate() != null) {
                validationDto.setBirthDate(userDtoUpdate.getBirthDate());
            }
            if (userDtoUpdate.getGender() != null) {
                validationDto.setGender(userDtoUpdate.getGender());
            }
            log.info("Валидация данных");
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

            if (userDtoUpdate.getUsername() == null || userDtoUpdate.getUsername().isBlank()) {
                Set<ConstraintViolation<UserDtoRegister>> violations = validator.validateProperty(
                        validationDto, "username"
                );
                if (!violations.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(violations.iterator().next().getMessage());
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

            if (userDtoUpdate.getGender() != null || userDtoUpdate.getGender() == null) {
                Set<ConstraintViolation<UserDtoRegister>> violations = validator.validateProperty(
                        validationDto, "gender"
                );
                if (!violations.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(violations.iterator().next().getMessage());
                }
            }
            if (userDtoUpdate.getBirthDate() != null || userDtoUpdate.getBirthDate() == null) {
                Set<ConstraintViolation<UserDtoRegister>> violations = validator.validateProperty(
                        validationDto, "birthDate"
                );
                if (!violations.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(violations.iterator().next().getMessage());
                }
            }

            int affectedRows = userService.updateUserProfile(BuilderUser.userDtoUpdateBuilder(userDtoUpdate));

            if (affectedRows == 0) {
                log.warn("Обновление пользователя с ID = {} не выполнено - запись не найдена", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }
            log.info("Обновление пользователя с ID = {} прошло успешно", userDtoUpdate.getId());
            user = userService.getUserById(userId);
            return ResponseEntity.ok(BuilderUser.userDtoRegisterBuilder(user.orElseThrow()));


        } catch (RuntimeException e) {
            if (e.getMessage().contains("unique")) {
                log.info("Пользователь с email = {} уже зарегистрирован {} ", userDtoUpdate.getEmail(), e.getMessage());
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
        List<User> users = userService.getUserAll();
        if (users.isEmpty()) {
            log.info("Пользователи отсутствуют");
            return ResponseEntity.noContent().build();

        }
        return ResponseEntity.ok(users.stream().map(BuilderUser::userDtoRegisterBuilder));
    }

    /**
     * Получение пользователя по ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable @Min(1) Long userId) {
        Optional<User> user = userService.getUserById(userId);
        ResponseEntity<?> validationResult = ValidatorUser.userNotFoundAndForbidden(
                user.orElse(null),
                userId);

        if (!validationResult.getStatusCode().is2xxSuccessful()) {
            return validationResult;
        }
        return ResponseEntity.ok(BuilderUser.userDtoRegisterBuilder(user.orElseThrow()));
    }

    /**
     * Удаление пользователя по ID
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable @Min(1) Long userId) {
        Optional<User> user = userService.getUserById(userId);
        ResponseEntity<?> validationResult = ValidatorUser.userNotFoundAndForbidden(
                user.orElse(null),
                userId);

        if (!validationResult.getStatusCode().is2xxSuccessful()) {
            return validationResult;
        }

        userService.deleteUserById(userId);
        user = userService.getUserById(userId);
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        log.debug("Ошибка удаления пользователя");
        return new ResponseEntity<>(HttpStatus.CONFLICT);

    }

}