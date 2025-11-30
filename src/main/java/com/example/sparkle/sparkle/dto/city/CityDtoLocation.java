package com.example.sparkle.sparkle.dto.city;

import com.example.sparkle.sparkle.model.City;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;


import java.util.Objects;

@Getter
@Setter
@Builder
public class CityDtoLocation {
    private Double latitude;
    private Double longitude;
    private String cityName;
    private String source;
    private Integer accuracy;


    public static CityDtoLocation toCityDtoLocation(City city) {
        Point point = city.getLocation();
        return CityDtoLocation.builder()
                    .latitude(point.getY())
                    .longitude(point.getX())
                    .cityName(city.getName())
                    .source(city.getSource())
                    .accuracy(city.getAccuracy())
                    .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CityDtoLocation)) return false;
        CityDtoLocation that = (CityDtoLocation) o;
        return Objects.equals(getLatitude(), that.getLatitude()) && Objects.equals(getLongitude(), that.getLongitude()) && Objects.equals(getCityName(), that.getCityName()) && Objects.equals(getSource(), that.getSource()) && Objects.equals(getAccuracy(), that.getAccuracy());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLatitude(), getLongitude(), getCityName(), getSource(), getAccuracy());
    }
}
