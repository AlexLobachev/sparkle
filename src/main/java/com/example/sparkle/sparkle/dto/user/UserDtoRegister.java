package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class UserDtoRegister {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    private String username;
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 255)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$",
            message = "Пароль должен содержать минимум 8 символов, включая буквы и цифры")
    private String password;
    @NotBlank(message = "Гендер не может быть пустым")
    private String gender;
    @Email(message = "Email введен не корректно данные должны быть в формате (abcabc@.abc.abc)")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
    @Past(message = "Дата рождения не может быть в будущем или настоящем")
    private LocalDate birthDate;

}
