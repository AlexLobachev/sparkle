package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.Gender;
import com.example.sparkle.sparkle.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
@Getter
@Setter
@ToString
public class UserDtoUpdate {
    private Long id;
    private String username;
    private Gender gender;
    private Gender preferredGender;
    //@NotBlank(message = "Email обязателен и не может быть пустым или состоять только из пробелов")
    private String email;
    @Past(message = "Дата рождения не может быть в будущем или настоящем")
    private LocalDate birthDate;
    private String aboutMe;
    private boolean emailPending = true;
    private String city;



}
