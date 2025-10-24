package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.Gender;
import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserInterestsDto {

    private Long userId;
    private List<Interest> interests;



}
