package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.LocationRequestDto;
import com.example.sparkle.sparkle.dto.user.UserDtoRegister;
import com.example.sparkle.sparkle.dto.user.UserDtoUpdate;
import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.service.GeocodingService;
import com.example.sparkle.sparkle.service.UserService;
import com.example.sparkle.sparkle.validator.ValidatorUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * Класс-контроллер для работы с пользователями
 */
@RestController
@RequestMapping("/sparkle/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final GeocodingService geocodingService;
    private final ValidatorUser validatorUser;

    @Autowired
    public UserController(UserService userService, GeocodingService geocodingService, ValidatorUser validatorUser) {
        this.userService = userService;
        this.geocodingService = geocodingService;
        this.validatorUser = validatorUser;
    }

    /**
     * Регистрация нового пользователя
     * Пользователь вводит Имя, Пол, Дату рождения
     */
    @PostMapping("/register/next-page")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDtoRegister userDtoRegister) {
        User user = userService.registerUser(UserDtoRegister.toUser(userDtoRegister)).orElseThrow();
        return ResponseEntity.ok(User.toUserDtoRegister(user));

    }

    /**
     * Редактирование профиля пользователя
     */
    @PatchMapping("/update-profile/{userId}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable @Min(1) Long userId,
            @Valid @RequestBody UserDtoUpdate userDtoUpdate) {
        return ResponseEntity.ok(User.toUserDtoBuilder(userService.updateUserProfile(userId, userDtoUpdate)
                .orElseThrow()));
    }

    /**
     * Получение всех пользователей
     */
    @GetMapping
    public ResponseEntity<?> getUserAll() {
        List<User> users = userService.getUserAll();
        return ResponseEntity.ok(users.stream().map(User::toUserDtoBuilder));
    }

    /**
     * Получение пользователя по ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable @Min(1) Long userId) {
        User user = userService.getUserById(userId).orElseThrow();
        return ResponseEntity.ok(User.toUserDtoBuilder(user));

    }

    /**
     * Удаление пользователя по ID
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable @Min(1) Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Сохранение локации
     */
    //@PostMapping("/location")
    @PostMapping("/location/{userId}")
    public ResponseEntity<?> saveUserLocation(@RequestBody LocationRequestDto location, @PathVariable Long userId
            /*@AuthenticationPrincipal UserDetails userDetails*/) {

        return ResponseEntity.ok(User.toUserDtoBuilder(userService.saveUserLocation(location, userId).orElseThrow()));
    }


}