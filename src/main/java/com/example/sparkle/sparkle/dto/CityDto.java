package com.example.sparkle.sparkle.dto;

import com.example.sparkle.sparkle.model.City;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CityDto {
    private Long cityId;
    private String cityName;

    public static CityDto toCityDto(City city) {
        if (city == null) {
            return null;
        } else {
            return CityDto.builder()
                    .cityId(city.getId())
                    .cityName(city.getName())
                    .build();
        }
    }
}
