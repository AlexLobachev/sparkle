package com.example.sparkle.sparkle.validator;

import com.example.sparkle.sparkle.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class ValidatorUser {

    public static ResponseEntity<?> userNotFoundAndForbidden(User user, Long userId) {
        if (user == null) {
            log.debug("Пользователь с ID = {} не найден", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Пользователь не найден");
        }
        if (!user.getId().equals(userId)) {
            log.debug("Пользователь с ID = {} не имеет доступа к ID = {}", userId, user.getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Доступ запрещен");
        }
        return ResponseEntity.ok(user);
    }
}
