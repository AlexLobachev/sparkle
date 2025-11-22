package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
@Getter
@Setter
@ToString
public class UserDtoUpdateValidator {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 50, message = "Длина логина должна быть от 2 до 50 символов")
    private String username;
    @NotNull(message = "Гендер обязателен к заполнению")
    private Gender gender;
    @NotNull(message = "Предпочтительный пол обязателен к заполнению")
    private Gender preferredGender;
    @Email(message = "Email введен не корректно данные должны быть в формате (mail@mail.ru)")
    private String email;
    @Past(message = "Дата рождения не может быть в будущем или настоящем")
    private LocalDate birthDate;
    @JsonIgnore
    @Size(max = 200, message = "Максимальная длина сообщения не может быть больше 200 символов")
    private String aboutMe;
}
