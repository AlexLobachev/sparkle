package com.example.sparkle.sparkle.validator;

import com.example.sparkle.sparkle.dto.user.*;
import com.example.sparkle.sparkle.exception.*;
import com.example.sparkle.sparkle.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

    public void userNoContent(List<UserDto> user) {
        if (user.isEmpty()) {
            log.info("Пришел пустой массив");
            throw new NoContent();
        }

    }

    public UserDtoUpdate invalidRequest(User user, UserDtoUpdate userDtoUpdate) {
        UserDtoUpdateValidator userDtoUpdateValidator = UserMapper.userDtoUpdateValidator(user);

        if (userDtoUpdate.getGender() != null) userDtoUpdateValidator.setGender(userDtoUpdate.getGender());
        else
            userDtoUpdateValidator.setGender(user.getGender());
        if (userDtoUpdate.getPreferredGender() != null)
            userDtoUpdateValidator.setPreferredGender(userDtoUpdate.getPreferredGender());
        else
            userDtoUpdateValidator.setPreferredGender(user.getGender());
        if (userDtoUpdate.getEmail() != null) userDtoUpdateValidator.setEmail(userDtoUpdate.getEmail());
        if (userDtoUpdate.getBirthDate() != null) userDtoUpdateValidator.setBirthDate(userDtoUpdate.getBirthDate());
        if (userDtoUpdate.getAboutMe() != null) userDtoUpdateValidator.setAboutMe(userDtoUpdate.getAboutMe());
        log.debug("userDtoUpdateValidator>>>>>>>" +userDtoUpdateValidator);
        log.info("Валидация данных");
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


        Set<ConstraintViolation<UserDtoUpdateValidator>> violations = validator.validate(userDtoUpdateValidator);

        if (!violations.isEmpty()) {
            // Собираем ошибки
            List<String> errors = violations.stream()
                    .map(ConstraintViolation::getMessage).toList();
            throw new ValidationException("Ошибки валидации: " + errors);
        }
        return UserMapper.userDtoUpdateValidator(userDtoUpdateValidator);

    }


    public void invalidEmail(DataIntegrityViolationException ex) {
            // Переводим SQLException в более конкретный тип
            Throwable rootCause = ex.getRootCause();
            if (rootCause instanceof java.sql.SQLException sqlEx) {
                String sqlState = sqlEx.getSQLState();
                String errorCode = sqlEx.getErrorCode() + "";

                // Для PostgreSQL: SQL State 23514 = check_violation
                if ("23514".equals(sqlState)) {
                    if (ex.getMessage().contains("valid_email_check")) {
                        throw new BadRequest(
                                "Некорректный email: должен быть заполнен и соответствовать формату name@domain.com"
                        );
                    }
                }
            }
            // Если не наша ошибка — перебрасываем как есть
            throw ex;
        }
    }




