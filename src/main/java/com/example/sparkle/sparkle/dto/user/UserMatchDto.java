package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.dto.CityDto;
import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.Gender;
import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class UserMatchDto {
    private Long userId;
    private String username;
    private Gender gender;
    private LocalDate birthDate;
    private String aboutMe;
    private CityDto city;
    private List<Interest> interests = new ArrayList<>();
    private List<PhotoDto> photos = new ArrayList<>();


    public static UserMatchDto toUserMatchDto(User user) {
        List<Interest> interestList = new ArrayList<>();
        List<PhotoDto> photoList = new ArrayList<>();
        if (user.getInterests() != null) {
            user.getInterests().forEach(interest -> interestList.add(interest.getInterest()));
        }
       if (user.getPhotos() != null) {
           user.getPhotos().forEach(photo -> photoList.add(PhotoDto.toPhotoDto(photo)));
       }
        return UserMatchDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .aboutMe(user.getAboutMe())
                .city(CityDto.toCityDto(user.getCity()))
                .interests(interestList)
                .photos(photoList)
                .build();
    }



}
