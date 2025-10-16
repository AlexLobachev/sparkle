package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserDtoGetAuthenticateUser {
    @NotBlank(message = "Имя не может быть пустым")
    private String username;
    private Gender gender;
    @Email(message = "Email введен не корректно данные должны быть в формате (abcabc@.abc.abc)")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
    @Past(message = "Дата рождения не может быть в будущем или настоящем")
    private LocalDate birthDate;
}
