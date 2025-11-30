package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.dto.city.CityDto;
import com.example.sparkle.sparkle.dto.photo.PhotoDto;
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
@ToString
@Builder
public class UserDto {
    private Long userId;
    private String username;
    private LocalDate birthDate;
    private CityDto city;
    private String aboutMe;
    private List<PhotoDto> photos = new ArrayList<>();
    private String email;
    private Gender gender;
    private Gender preferredGender;
    private List<Interest> interests = new ArrayList<>();

    public static UserDto toUserDto(User user) {
        List<Interest> interestList = new ArrayList<>();
        List<PhotoDto> photoList = new ArrayList<>();
        if (user.getInterests() != null) {
            user.getInterests().forEach(interest -> interestList.add(interest.getInterest()));
        }
        if (user.getPhotos() != null) {
            user.getPhotos().forEach(photo -> photoList.add(PhotoDto.toPhotoDto(photo)));
        }
        return UserDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .birthDate(user.getBirthDate())
                .city(CityDto.toCityDto(user.getCity()))
                .aboutMe(user.getAboutMe())
                .photos(photoList)
                .email(user.getEmail())
                .gender(user.getGender())
                .preferredGender(user.getPreferredGender())
                .interests(interestList)
                .build();
    }

}
