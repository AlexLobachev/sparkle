package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.dto.CityDto;
import com.example.sparkle.sparkle.model.Gender;
import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.UserPhoto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class UserMatchDto {
    private Long id;
    private String username;
    private Gender gender;
    private LocalDate birthDate;
    private String aboutMe;
    private CityDto cityDto;
    private List<Interest> interests = new ArrayList<>();
    private List<UserPhoto> photos = new ArrayList<>();
}
