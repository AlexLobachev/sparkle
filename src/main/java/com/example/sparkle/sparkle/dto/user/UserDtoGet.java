package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.Gender;
import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.UserPhoto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Getter
@Setter
@ToString
public class UserDtoGet {
    private String username;
    private String email;
    private String gender;
    private LocalDate birthDate;
    private City city;
    private Set<Interest> interests = new HashSet<>();
    private List<UserPhoto> photos = new ArrayList<>();
}
