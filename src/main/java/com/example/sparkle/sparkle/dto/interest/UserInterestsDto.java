package com.example.sparkle.sparkle.dto.interest;

import com.example.sparkle.sparkle.model.Gender;
import com.example.sparkle.sparkle.model.Interest;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserInterest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class UserInterestsDto {
    private Long userId;
    private List<Interest> interests;


    /**
     * Преобразует список интересов в DTO
     */
    public static UserInterestsDto toUserInterestDto(List<UserInterest> userInterest) {
        if (userInterest == null || userInterest.isEmpty()) {
            throw new IllegalArgumentException("Список интересов не может быть пустым");
        }
        return UserInterestsDto.builder()
                .userId(userInterest.get(0).getUser().getId())
                .interests(userInterest.stream().map(UserInterest::getInterest).toList()).build();
    }
}
