package com.example.sparkle.sparkle.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserDtoAuthenticate {
    private Long id;
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 255, message = "Пароль не должен быть короче 6 символов и больше 255")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$",
            message = "Пароль должен содержать буквы и цифры")
    private String password;
    @Email(message = "Email введен не корректно данные должны быть в формате (mail@.mail.ru)")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
}
