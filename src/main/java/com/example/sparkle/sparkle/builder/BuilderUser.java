package com.example.sparkle.sparkle.builder;

import com.example.sparkle.sparkle.dto.user.UserDto;
import com.example.sparkle.sparkle.dto.user.UserDtoRegister;
import com.example.sparkle.sparkle.dto.user.UserDtoUpdate;
import com.example.sparkle.sparkle.model.User;

public class BuilderUser {
    public static User userBuilder(UserDtoRegister userDtoRegister) {
        User user = new User();
        user.setId(userDtoRegister.getId());
        user.setUsername(userDtoRegister.getUsername());
        user.setGender(userDtoRegister.getGender());
        user.setBirthDate(userDtoRegister.getBirthDate());
        user.setEmail(userDtoRegister.getEmail());
        return user;
    }

    public static User userDtoUpdateBuilder(UserDtoUpdate userDtoUpdate) {
        User user = new User();
        user.setId(userDtoUpdate.getId());
        user.setUsername(userDtoUpdate.getUsername());
        user.setGender(userDtoUpdate.getGender());
        user.setBirthDate(userDtoUpdate.getBirthDate());
        user.setEmail(userDtoUpdate.getEmail());
        return user;
    }

    public static UserDtoRegister userDtoRegisterBuilder(User user) {
        UserDtoRegister userDtoRegister = new UserDtoRegister();
        userDtoRegister.setId(user.getId());
        userDtoRegister.setUsername(user.getUsername());
        userDtoRegister.setGender(user.getGender());
        userDtoRegister.setBirthDate(user.getBirthDate());
        userDtoRegister.setEmail(user.getEmail());
        return userDtoRegister;
    }

    public static UserDto userDtoBuilder(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setGender(user.getGender());
        userDto.setCity(user.getCity());
        userDto.setBirthDate(user.getBirthDate());
        userDto.setEmail(user.getEmail());
        userDto.setInterests(user.getInterests());
        userDto.setPhotos(user.getPhotos());
        return userDto;
    }
}
