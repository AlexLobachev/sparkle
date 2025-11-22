package com.example.sparkle.sparkle.model;

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

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;


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
