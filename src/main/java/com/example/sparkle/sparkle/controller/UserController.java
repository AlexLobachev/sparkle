package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.user.UserDTO;
import com.example.sparkle.sparkle.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDto) {
        return ResponseEntity.ok(userService.registerUser(userDto));
    }

    /**
     * Аутентификация пользователя
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserDTO userDto) {
        return ResponseEntity.ok(userService.authenticateUser(userDto));
    }

    /**
     * Получение информации о текущем авторизованном пользователе
     */
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getCurrentUserProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile()).getBody();
    }

    /**
     * Редактирование профиля пользователя
     */
    @PatchMapping("/update-profile")
    public ResponseEntity<UserDTO> updateUserProfile(@Valid @RequestBody UserDTO updatedUserDto) {
        return ResponseEntity.ok(userService.updateUserProfile(updatedUserDto)).getBody();
    }

    /**
     * Загрузка фотографии пользователя
     */
    @PostMapping("/upload-photo")
    public ResponseEntity<String> uploadUserPhoto(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadUserPhoto(file)).getBody();
    }

    /**
     * Удаление фотографии пользователя
     */
    @DeleteMapping("/remove-photo/{photoId}")
    public ResponseEntity<Void> removeUserPhoto(@PathVariable Long photoId) {
        userService.removeUserPhoto(photoId);
        return ResponseEntity.noContent().build();
    }
}