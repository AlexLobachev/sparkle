package com.example.sparkle.sparkle.dto.city;

import com.example.sparkle.sparkle.model.City;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CityDtoName {
    String cityName;

    public static CityDtoName toCityDtoName(City city) {
        return CityDtoName.builder()
                .cityName(city.getName())
                .build();
    }
}
