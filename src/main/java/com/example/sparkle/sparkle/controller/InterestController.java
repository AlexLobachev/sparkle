package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserInterest;
import com.example.sparkle.sparkle.service.UserInterestService;
import com.example.sparkle.sparkle.service.UserService;
import com.example.sparkle.sparkle.validator.ValidatorUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sparkle/users/interests")
@Slf4j
public class InterestController {

    private final UserInterestService userInterestService;
    private final UserService userService;

    @Autowired
    public InterestController(UserInterestService userInterestService, UserService userService) {
        this.userInterestService = userInterestService;
        this.userService = userService;
    }


    /**
     * Сохраняем интересы пользователю
     */
    @PostMapping("/create-all/users/{userId}")
    public ResponseEntity<?> saveInterest(@PathVariable Long userId, @RequestBody List<UserInterest> listInterest) {
        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            return new ResponseEntity<>("Пользователь не найден", HttpStatus.NOT_FOUND);
        }

        // Получаем текущие интересы пользователя
        List<UserInterest> existingInterests = userInterestService.getAllInterestUserById(userId);
        Set<Interest> existingInterestSet = existingInterests.stream()
                .map(UserInterest::getInterest)
                .collect(Collectors.toSet());
        // Фильтруем новые интересы, оставляя только уникальные
        List<UserInterest> newInterests = listInterest.stream()
                .filter(interest -> !existingInterestSet.contains(interest.getInterest()))
                .peek(interest -> {
                    interest.setUser(user.orElseThrow());
                    interest.setId(null); // сбрасываем ID для новой записи
                }).toList();
        // Возвращаем обновленный список интересов

        if (!newInterests.isEmpty()) {
            userInterestService.saveAllInterest(newInterests);
        }
        List<UserInterest> updatedInterests = userInterestService.getAllInterestUserById(userId);
        if (updatedInterests.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        log.debug("updatedInterests >>>>>> " + updatedInterests);
        return ResponseEntity.ok()
                .body(UserInterest.toUserInterestDto(updatedInterests));
    }


    @PostMapping("/create/users/{userId}")
    public ResponseEntity<?> saveInterest(@PathVariable Long userId, @RequestBody UserInterest interest) {
        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            return new ResponseEntity<>("Пользователь не найден", HttpStatus.NOT_FOUND);
        }
        interest.setUser(user.orElseThrow());
        if (userInterestService.getAllInterestUserById(userId)
                .stream()
                .anyMatch(userInterest -> userInterest.getInterest().equals(interest.getInterest()))) {
            interest.setUser(user.orElseThrow());
            return ResponseEntity.ok(User.toUserDtoBuilder(UserInterest.toUser(userInterestService.getInterestUserById(interest))));
        }
        return ResponseEntity.ok(User.toUserDtoBuilder(UserInterest.toUser(userInterestService.saveInterest(interest))));
    }


    /**
     * Получаем все интересы пользователя по его ID
     */
    @GetMapping("/users/interests-all/{userId}")
    public ResponseEntity<?> getAllInterestUserById(@PathVariable Long userId) {
        Optional<User> user = userService.getCurrentUserProfile(userId);
        ResponseEntity<?> validationResult = ValidatorUser.userNotFoundAndForbidden(
                user.orElse(null),
                userId);

        if (!validationResult.getStatusCode().is2xxSuccessful()) {
            return validationResult;
        }
        //List<UserInterest> userInterests = userInterestService.getAllInterestUserById(userId);


        return ResponseEntity.ok(UserInterest.toUserInterestDto(userInterestService.getAllInterestUserById(userId)));
    }

    @GetMapping(value = "/users/interests-all/general-interest/{userId}")
    public ResponseEntity<?> getUsersWithTheSameInterests(@PathVariable Long userId) {

        log.debug("userId -> " + userId);
        if (userId != null) {
            Optional<User> user = userService.getCurrentUserProfile(userId);
            ResponseEntity<?> validationResult = ValidatorUser.userNotFoundAndForbidden(
                    user.orElse(null),
                    userId);

            if (!validationResult.getStatusCode().is2xxSuccessful()) {
                return validationResult;
            }
        }
        return ResponseEntity.ok(userInterestService.getUsersWithTheSameInterests(userId)
                .stream()
                .filter(user -> !user.getId().equals(userId))
                .map(User::toUserDtoRegister));
    }

    @GetMapping(value = "/users/interests-all/general-interest")
    public ResponseEntity<?> getUsersWithTheSameInterests() {
        return ResponseEntity.ok(userInterestService.getUsersWithTheSameInterests());
    }

    @DeleteMapping("/create/users/{userId}")
    ResponseEntity<?> deleteInterestByUserId(@PathVariable Long userId, @RequestBody UserInterest interest) {
        Optional<User> user = userService.getCurrentUserProfile(userId);
        if (userId != null) {
            ResponseEntity<?> validationResult = ValidatorUser.userNotFoundAndForbidden(
                    user.orElse(null),
                    userId);

            if (!validationResult.getStatusCode().is2xxSuccessful()) {
                return validationResult;
            }
        }

        interest.setUser(user.orElseThrow());
        log.debug("interest = " + interest);
        userInterestService.deleteInterestByUserId(interest);
        interest = userInterestService.getInterestUserById(interest);
        if (interest == null) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        log.debug("Ошибка удаления интереса");
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }


}
