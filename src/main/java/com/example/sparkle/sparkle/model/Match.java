package com.example.sparkle.sparkle.model;

import com.example.sparkle.sparkle.dto.MatchDto;
import com.example.sparkle.sparkle.dto.user.UserMatchDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "matches")
@Getter
@Setter
@Slf4j
@ToString
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_user_id")
    private User firstUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_user_id")
    private User secondUser;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "match_status")
    private Boolean matchStatus = false;

    public static MatchDto toMatchDto(Match match) {
        MatchDto matchDto = new MatchDto();
        matchDto.setMatchId(match.getId());
        matchDto.setUser(toUserMatchDto(match.getSecondUser()));
        return matchDto;
    }

    public static MatchDto toMatchDto(UserMatchDto userMatchDto) {
        MatchDto matchDto = new MatchDto();
        matchDto.setMatchId(userMatchDto.getId());
        matchDto.setUser(userMatchDto);
        return matchDto;
    }

    public static UserMatchDto toUserMatchDto(User user) {
        List<Interest> interestList = new ArrayList<>();
        if (user.getInterests() != null)
            user.getInterests().forEach(interest -> interestList.add(interest.getInterest()));
        UserMatchDto userMatchDto = new UserMatchDto();
        userMatchDto.setId(user.getId());
        userMatchDto.setUsername(user.getUsername());
        userMatchDto.setGender(user.getGender());
        userMatchDto.setBirthDate(user.getBirthDate());
        userMatchDto.setAboutMe(user.getAboutMe());
        if (user.getCity() != null)
            userMatchDto.setCityDto(City.cityDto(user.getCity()));
        userMatchDto.setInterests(interestList);
        userMatchDto.setPhotos(user.getPhotos());
        return userMatchDto;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match match1)) return false;
        return getId().equals(match1.getId()) && getFirstUser().equals(match1.getFirstUser()) && getSecondUser().equals(match1.getSecondUser()) && getCreatedAt().equals(match1.getCreatedAt()) && getMatchStatus().equals(match1.getMatchStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstUser(), getSecondUser(), getCreatedAt(), getMatchStatus());
    }
}
