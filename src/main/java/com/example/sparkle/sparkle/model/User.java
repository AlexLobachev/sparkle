package com.example.sparkle.sparkle.model;

import com.example.sparkle.sparkle.dto.user.UserDto;
import com.example.sparkle.sparkle.dto.user.UserDtoGetAllInterest;
import com.example.sparkle.sparkle.dto.user.UserDtoRegister;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Column(nullable = false)
    private String username;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_gender")
    private Gender preferredGender;
    @Temporal(TemporalType.DATE)
    @Column(name = "birth_date")
    private LocalDate birthDate;
    @Column(name = "about_me")
    @Size(max = 200, message = "Максимальная длина сообщения не может быть больше 200 символов")
    private String aboutMe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    @ToString.Exclude
    private City city;
    // Список фотографий пользователя
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<UserPhoto> photos = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<UserInterest> interests = new ArrayList<>();

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

    public static UserDto toUserDtoBuilder(User user) {
        List<Interest> interestList = new ArrayList<>();
        user.getInterests().forEach(interest -> interestList.add(interest.getInterest()));
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setGender(user.getGender());
        userDto.setPreferredGender(user.getPreferredGender());
        userDto.setBirthDate(user.getBirthDate());
        userDto.setAboutMe(user.getAboutMe());
        userDto.setEmail(user.getEmail());
        userDto.setInterests(interestList);
        if (user.getCity() != null)
            userDto.setCityDto(City.cityDto(user.getCity()));

        return userDto;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return getId().equals(user.getId()) && getUsername().equals(user.getUsername()) && getEmail().equals(user.getEmail()) && getGender().equals(user.getGender()) && getBirthDate().equals(user.getBirthDate()) && getCity().equals(user.getCity()) /*&& getInterests().equals(user.getInterests())*/ && getPhotos().equals(user.getPhotos());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUsername(), getEmail(), getGender(), getBirthDate(), getCity(), /*getInterests(),*/ getPhotos());
    }
}
