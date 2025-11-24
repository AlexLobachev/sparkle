package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.user.UserDto;
import com.example.sparkle.sparkle.dto.user.UserMapper;
import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.UserInterest;
import com.example.sparkle.sparkle.service.UserInterestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/create-all")
    public ResponseEntity<?> saveAllInterest(@RequestBody List<UserInterest> listInterest) {
        List<UserInterest> userInterests = userInterestService.saveAllInterest(listInterest);
        List<String> interestName = new ArrayList<>();
        userInterests.forEach(inter->interestName.add(inter.getInterest().getLabel()));
        return ResponseEntity.ok()
                .body(interestName);
    }


    /**
     * Получаем все интересы пользователя по его ID
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getAllInterestUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(UserInterest.toUserInterestDto(
                userInterestService.getAllInterestUserById(userId)));
    }

    /**
     * Получаем всех пользователей с общими интересами как у пользователя по ID
     */
    @GetMapping(value = "/general-interest/{userId}")
    public ResponseEntity<?> getUsersWithTheSameInterests(@PathVariable Long userId) {
        return ResponseEntity.ok(userInterestService.getUsersWithTheSameInterests(userId)
                .stream()
                .filter(user -> !user.getId().equals(userId))
                .map(UserDto::toUserDto));
    }

    /**
     * Получаем всех пользователей с общими интересами
     */
    @GetMapping(value = "/general-interest")
    public ResponseEntity<?> getAllUsersWithTheSameInterests() {
        return ResponseEntity.ok(userInterestService.getAllUsersWithTheSameInterests());
    }

    /**
     * Удаление интереса
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/delete/{interestKey}")
    ResponseEntity<?> deleteInterestByUserId(@PathVariable String interestKey) {
        userInterestService.deleteInterestByUserId(interestKey);
        return ResponseEntity.ok().build();
    }

    /**
     * Получение списка интересов
     */
   @PreAuthorize("hasRole('ROLE_USER')")
   @GetMapping("/all")
   public ResponseEntity<Map<String, String>> getInterestLabels() {
       return ResponseEntity.ok(
               Arrays.stream(Interest.values())
                       .collect(Collectors.toMap(Enum::name, Interest::getLabel))
       );
   }





}
