package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
@Builder
public class UserDTO {
    private Long id;
    @NotBlank
    private String username;
    private Gender gender;
    @Past
    private LocalDate birthDate;
    private City city;
}
