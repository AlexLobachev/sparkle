package com.example.sparkle.sparkle.dto.user;

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
    @Size(min = 2, max = 50, message = "Длина имени должна быть от 2 до 50 символов")
    private String username;
    @NotBlank(message = "Гендер не может быть пустым")
    private String gender;
    @Email(message = "Email введен не корректно данные должны быть в формате (mail@mail.ru)")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
    @Past(message = "Дата рождения не может быть в будущем или настоящем")
    private LocalDate birthDate;

}
