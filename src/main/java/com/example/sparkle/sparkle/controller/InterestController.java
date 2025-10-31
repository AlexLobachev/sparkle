package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserInterest;
import com.example.sparkle.sparkle.service.UserInterestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Класс-контроллер для работы с интересами пользователя
 */
@RestController
@RequestMapping("/sparkle/users/interests")
@Slf4j
public class InterestController {

    private final UserInterestService userInterestService;

    @Autowired
    public InterestController(UserInterestService userInterestService) {
        this.userInterestService = userInterestService;


    }


    /**
     * Сохраняем интересы пользователю списком
     */

    @PostMapping("/create-all/users/{userId}")
    public ResponseEntity<?> saveAllInterest(@PathVariable Long userId, @RequestBody List<UserInterest> listInterest) {
        List<UserInterest> userInterests = userInterestService.saveAllInterest(userId, listInterest);
        return ResponseEntity.ok()
                .body(UserInterest.toUserInterestDto(userInterests));
    }

    /**
     * Сохраняем интересы пользователю по одному
     */

    @PostMapping("/create/users/{userId}")
    public ResponseEntity<?> saveInterest(@PathVariable Long userId, @RequestBody UserInterest interest) {
        userInterestService.saveInterest(userId, interest);
        List<UserInterest> userInterests = userInterestService.getAllInterestUserById(userId);
        return ResponseEntity.ok(UserInterest.toUserInterestDto(userInterests));
    }


    /**
     * Получаем все интересы пользователя по его ID
     */
    @GetMapping("/users/interests-all/{userId}")
    public ResponseEntity<?> getAllInterestUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(UserInterest.toUserInterestDto(
                userInterestService.getAllInterestUserById(userId)));
    }

    /**
     * Получаем всех пользователей с общими интересами как у пользователя по ID
     */
    @GetMapping(value = "/users/interests-all/general-interest/{userId}")
    public ResponseEntity<?> getUsersWithTheSameInterests(@PathVariable Long userId) {
        return ResponseEntity.ok(userInterestService.getUsersWithTheSameInterests(userId)
                .stream()
                .filter(user -> !user.getId().equals(userId))
                .map(User::toUserDtoBuilder));
    }

    /**
     * Получаем всех пользователей с общими интересами
     */
    @GetMapping(value = "/users/interests-all/general-interest")
    public ResponseEntity<?> getAllUsersWithTheSameInterests() {
        return ResponseEntity.ok(userInterestService.getAllUsersWithTheSameInterests());
    }

    /**
     * Удаление интереса
     */
    @DeleteMapping("/create/users/{userId}")
    ResponseEntity<?> deleteInterestByUserId(@PathVariable Long userId, @RequestBody UserInterest interest) {
        userInterestService.deleteInterestByUserId(userId, interest);
        return ResponseEntity.ok().build();

    }


}
