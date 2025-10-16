package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.Gender;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserDtoRegister {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    private String username;
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль не должен быть короче 6 символов")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$",
            message = "Пароль должен содержать буквы и цифры")
    private String password;
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$",
            message = "Пароль должен содержать буквы и цифры")
    private String oldPassword;
    private Gender gender;
    @Email(message = "Email введен не корректно данные должны быть в формате (abcabc@.abc.abc)")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
    @Past(message = "Дата рождения не может быть в будущем или настоящем")
    private LocalDate birthDate;

}
