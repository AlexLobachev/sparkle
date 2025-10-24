package com.example.sparkle.sparkle.dto.user;

import com.example.sparkle.sparkle.model.Gender;
import com.example.sparkle.sparkle.model.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
@Getter
@Setter
@ToString
public class UserDtoUpdate {
    private Long id;
    private String username;
    private Gender gender;
    private String email;
    private LocalDate birthDate;
    private String aboutMe;

    public static User toUser(UserDtoUpdate userDtoUpdate) {
        User user = new User();
        user.setId(userDtoUpdate.getId());
        user.setUsername(userDtoUpdate.getUsername());
        user.setGender(userDtoUpdate.getGender());
        user.setBirthDate(userDtoUpdate.getBirthDate());
        user.setEmail(userDtoUpdate.getEmail());
        user.setAboutMe(userDtoUpdate.getAboutMe());
        return user;
    }
}
