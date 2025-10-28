package com.example.sparkle.sparkle.model;

import com.example.sparkle.sparkle.dto.CityDto;
import jakarta.persistence.*;
import lombok.*;

import org.locationtech.jts.geom.Point;

import java.util.Objects;

@Entity
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_name")
    private String name;

    // Координаты города для точного вычисления расстояния
    //@Column(name = "location",columnDefinition = "GEOGRAPHY(Point, 4326)", nullable = false)
    //@Embedded
    //@Column(name = "location")

    //@Transient
    //@Column(name = "location",columnDefinition = "geometry(Point,4326)")
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    public static CityDto cityDto (City city){
        CityDto cityDto = new CityDto();
        cityDto.setId(city.getId());
        cityDto.setCityName(city.getName());
        return cityDto;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof City city)) return false;
        return id.equals(city.id) && name.equals(city.name) && location.equals(city.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, location);
    }
}
