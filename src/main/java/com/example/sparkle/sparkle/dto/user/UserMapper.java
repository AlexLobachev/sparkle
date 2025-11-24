package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.dto.CityDto;
import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserMapper {

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







}
