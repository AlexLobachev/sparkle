package com.example.sparkle.sparkle.model;

import com.example.sparkle.sparkle.dto.user.UserInterestsDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user_interests")
public class UserInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(name = "interest", nullable = false)
    private Interest interest;

    public static UserInterestsDto toUserInterestDto(List<UserInterest> userInterest) {

        UserInterestsDto userInterestsDto = new UserInterestsDto();
        userInterestsDto.setUserId(userInterest.stream().findFirst().orElseThrow().getUser().getId());
        List<Interest> interestList = new ArrayList<>();
        userInterest.forEach(interest -> interestList.add(interest.getInterest()));
        userInterestsDto.setInterests(interestList);
        return userInterestsDto;
    }

    public static User toUser(UserInterest interest) {
        User user = new User();
        user.setId(interest.getUser().getId());
        user.setUsername(interest.getUser().getUsername());
        user.setGender(interest.getUser().getGender());
        user.setBirthDate(interest.getUser().getBirthDate());
        user.setEmail(interest.getUser().getEmail());
        user.setAboutMe(interest.getUser().getAboutMe());
        user.setInterests(interest.getUser().getInterests());
        return user;
    }

}
