package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Gender gender;
    private LocalDate birthDate;
    private String aboutMe;
    private City city;
    private List<Interest> interests = new ArrayList<>();
    private List<UserPhoto> photos = new ArrayList<>();
}
