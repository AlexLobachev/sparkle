package com.example.sparkle.sparkle.model;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.Objects;

/**
 * Сущность города с геометрической точкой для PostGIS.
 */
@Entity
@Table(name = "cities", indexes = {
        @Index(name = "idx_city_location", columnList = "location")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_name", nullable = false)
    private String name;

    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "source", nullable = false) // 'gps', 'wifi', 'ip', 'user_input'
    private String source;

    @Column(name = "accuracy", nullable = true) // В метрах
    private Integer accuracy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof City city)) return false;
        return Objects.equals(getId(), city.getId()) && Objects.equals(getName(), city.getName()) && Objects.equals(getLocation(), city.getLocation()) && Objects.equals(getSource(), city.getSource()) && Objects.equals(getAccuracy(), city.getAccuracy());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getLocation(), getSource(), getAccuracy());
    }
}