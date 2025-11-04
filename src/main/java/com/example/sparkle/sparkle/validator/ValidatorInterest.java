package com.example.sparkle.sparkle.validator;

import com.example.sparkle.sparkle.exception.Conflict;
import com.example.sparkle.sparkle.exception.NoContent;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserInterest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class ValidatorInterest {
    public void interestNoContent(List<UserInterest> user) {
        if (user.isEmpty()) {
            log.info("У пользователя нет интересов");
            throw new NoContent();
        }

    }
    public void interestConflictDelete(UserInterest interest) {
        if (interest != null) {
            log.warn("Ошибка удаления интереса");
            throw new Conflict("Ошибка удаления интереса");
        }
    }
}
