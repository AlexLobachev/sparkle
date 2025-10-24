package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.Gender;
import com.example.sparkle.sparkle.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Gender gender;
    @Email(message = "Email введен не корректно данные должны быть в формате (mail@mail.ru)")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
    @Past(message = "Дата рождения не может быть в будущем или настоящем")
    private LocalDate birthDate;
    @JsonIgnore
    @Size(max = 200, message = "Максимальная длина сообщения не может быть больше 200 символов")
    private String aboutMe;

    public static User toUser(UserDtoRegister userDtoRegister) {
        User user = new User();
        user.setId(userDtoRegister.getId());
        user.setUsername(userDtoRegister.getUsername());
        user.setGender(userDtoRegister.getGender());
        user.setBirthDate(userDtoRegister.getBirthDate());
        user.setEmail(userDtoRegister.getEmail());
        user.setAboutMe(userDtoRegister.getAboutMe());
        return user;
    }

}
