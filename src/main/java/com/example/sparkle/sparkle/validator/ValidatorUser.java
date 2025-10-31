package com.example.sparkle.sparkle.validator;

import com.example.sparkle.sparkle.dto.user.UserDtoRegister;
import com.example.sparkle.sparkle.dto.user.UserDtoUpdate;
import com.example.sparkle.sparkle.exception.*;
import com.example.sparkle.sparkle.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ValidatorUser {

    public void userNotFound(User user) {
        if (user == null) {
            log.info("Пользователь не найден");
            throw new NotFound("Пользователь не найден");
        }

    }

    public void userBadRequestEmail(User user) {
        if (user.getEmail() == null) {
            log.warn("Email отсутствует");
            throw new BadRequest("Email отсутствует");
        }

    }

    public void userForbidden(User user, Long userId) {
        if (!user.getId().equals(userId)) {
            log.info("Доступ запрещен, у вас нет прав доступа к пользователю с ID = " + user.getId());
            throw new Forbidden("Доступ запрещен, у вас нет прав доступа к пользователю с ID = " + user.getId());

        }
    }

    public void userConflictEmail(User user, User userValid) {
        if (userValid != null && user.getEmail().equals(userValid.getEmail())) {
            log.info("Попытка зарегистрироваться по существующему email = " + userValid.getEmail());
            throw new Conflict("Попытка зарегистрироваться по существующему email = " + userValid.getEmail());

        }


    }


    public void userConflictDelete(User user) {
        if (user != null) {
            log.warn("Ошибка удаления пользователя");
            throw new Conflict("Ошибка удаления пользователя");
        }
    }

    public void userNoContent(List<User> user) {
        if (user.isEmpty()) {
            log.info("Пришел пустой массив");
            throw new NoContent();
        }

    }

    public void invalidRequest(User user, UserDtoUpdate userDtoUpdate) {
        UserDtoRegister userDtoRegister = User.toUserDtoRegister(user);
        if (userDtoUpdate.getUsername() != null) userDtoRegister.setUsername(userDtoUpdate.getUsername());
        if (userDtoUpdate.getGender() != null) userDtoRegister.setGender(userDtoUpdate.getGender());
        if (userDtoUpdate.getPreferredGender() != null)
            userDtoRegister.setPreferredGender(userDtoUpdate.getPreferredGender());
        if (userDtoUpdate.getEmail() != null) userDtoRegister.setEmail(userDtoUpdate.getEmail());
        if (userDtoUpdate.getBirthDate() != null) userDtoRegister.setBirthDate(userDtoUpdate.getBirthDate());
        if (userDtoUpdate.getAboutMe() != null) userDtoRegister.setAboutMe(userDtoUpdate.getAboutMe());

        log.info("Валидация данных");
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


        Set<ConstraintViolation<UserDtoRegister>> violations = validator.validate(userDtoRegister);

        if (!violations.isEmpty()) {
            // Собираем ошибки
            List<String> errors = violations.stream()
                    .map(ConstraintViolation::getMessage).toList();
            throw new ValidationException("Ошибки валидации: " + errors);
        }

    }

}
