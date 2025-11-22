package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.dto.CityDto;
import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserPhoto;

import java.util.ArrayList;
import java.util.List;

public class UserMapper {
    public static User toUser(User user, UserDtoUpdate userDtoUpdate) {
        user.setGender(userDtoUpdate.getGender());
        user.setPreferredGender(userDtoUpdate.getPreferredGender());
        user.setEmail(userDtoUpdate.getEmail());
        user.setBirthDate(userDtoUpdate.getBirthDate());
        user.setAboutMe(userDtoUpdate.getAboutMe());
        return user;
    }

    public static User toUser(UserDtoRegister userDtoRegister) {
        User user = new User();
        user.setId(userDtoRegister.getId());
        user.setUsername(userDtoRegister.getUsername());
        user.setGender(userDtoRegister.getGender());
        user.setPreferredGender(userDtoRegister.getPreferredGender());
        user.setBirthDate(userDtoRegister.getBirthDate());
        user.setEmail(userDtoRegister.getEmail());
        user.setAboutMe(userDtoRegister.getAboutMe());
        return user;
    }

    public static UserDtoRegister toUserDtoRegister(User user) {
        UserDtoRegister userDtoRegister = new UserDtoRegister();
        userDtoRegister.setId(user.getId());
        userDtoRegister.setUsername(user.getUsername());
        userDtoRegister.setGender(user.getGender());
        userDtoRegister.setPreferredGender(user.getPreferredGender());
        userDtoRegister.setBirthDate(user.getBirthDate());
        userDtoRegister.setEmail(user.getEmail());
        userDtoRegister.setAboutMe(user.getAboutMe());
        return userDtoRegister;
    }

    public static UserDtoUpdateValidator userDtoUpdateValidator(User user) {
        UserDtoUpdateValidator userDtoUpdateValidator = new UserDtoUpdateValidator();
        userDtoUpdateValidator.setId(user.getId());
        userDtoUpdateValidator.setUsername(user.getUsername());
        userDtoUpdateValidator.setGender(user.getGender());
        userDtoUpdateValidator.setPreferredGender(user.getPreferredGender());
        userDtoUpdateValidator.setBirthDate(user.getBirthDate());
        userDtoUpdateValidator.setEmail(user.getEmail());
        userDtoUpdateValidator.setAboutMe(user.getAboutMe());
        return userDtoUpdateValidator;
    }

    public static UserDtoUpdate userDtoUpdateValidator(UserDtoUpdateValidator user) {
        UserDtoUpdate userDtoUpdate = new UserDtoUpdate();
        userDtoUpdate.setId(user.getId());
        userDtoUpdate.setUsername(user.getUsername());
        userDtoUpdate.setGender(user.getGender());
        userDtoUpdate.setPreferredGender(user.getPreferredGender());
        userDtoUpdate.setBirthDate(user.getBirthDate());
        userDtoUpdate.setEmail(user.getEmail());
        userDtoUpdate.setAboutMe(user.getAboutMe());
        return userDtoUpdate;
    }

    public static UserDto toUserDto(User user) {
        List<Interest> interestList = new ArrayList<>();
        List<UserPhotoDto> photoList = new ArrayList<>();
        user.getInterests().forEach(interest -> interestList.add(interest.getInterest()));
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setUsername(user.getUsername());
        userDto.setGender(user.getGender());
        userDto.setPreferredGender(user.getPreferredGender());
        userDto.setBirthDate(user.getBirthDate());
        userDto.setAboutMe(user.getAboutMe());
        userDto.setInterests(interestList);
        user.getPhotos().forEach(photo -> photoList.add(toUserPhotoDto(photo)));
        userDto.setPhotos(photoList);
        if (user.getCity() != null)
            userDto.setCity(toCityDto(user));

        return userDto;
    }

    public static UserDtoUpdate toUserDtoUpdate(User user) {
        UserDtoUpdate userDtoUpdate = new UserDtoUpdate();
        userDtoUpdate.setId(user.getId());
        userDtoUpdate.setUsername(user.getUsername());
        userDtoUpdate.setGender(user.getGender());
        userDtoUpdate.setPreferredGender(user.getPreferredGender());
        userDtoUpdate.setBirthDate(user.getBirthDate());
        userDtoUpdate.setAboutMe(user.getAboutMe());
        userDtoUpdate.setEmail(user.getEmail());
        userDtoUpdate.setEmailPending(user.isEmailPending());

        return userDtoUpdate;
    }

    public static UserPhotoDto toUserPhotoDto(UserPhoto userPhoto) {
        UserPhotoDto userPhotoDto = new UserPhotoDto();
        userPhotoDto.setId(userPhoto.getPhoto().getId());
        userPhotoDto.setUrl("/images/" + userPhoto.getPhoto().getFileName());
        userPhotoDto.setFileName(userPhoto.getPhoto().getFileName());
        return userPhotoDto;
    }

    public static User toUser(UserDto userDto) {
        User user = new User();
        City city = new City();
        if (userDto.getCity() != null) {
            city.setId(userDto.getCity().getCityId());
        }
        user.setId(userDto.getId());
        user.setUsername(user.getUsername());
        user.setGender(userDto.getGender());
        user.setPreferredGender(userDto.getPreferredGender());
        user.setBirthDate(userDto.getBirthDate());
        user.setAboutMe(user.getAboutMe());
        user.setCity(city);
        return user;
    }

    public static CityDto toCityDto(User user) {
        CityDto cityDto = new CityDto();
        cityDto.setCityName(user.getCity().getName());
        return cityDto;
    }

}
