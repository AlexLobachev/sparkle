package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class UserDtoUpdate {
    private Long id;
    private String username;
    private String gender;
    private String email;
    private LocalDate birthDate;
}
