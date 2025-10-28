package com.example.sparkle.sparkle.repository;

import com.example.sparkle.sparkle.model.City;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City,Long> {
    // Метод для поиска города по названию
     //@Query("SELECT c FROM City c WHERE c.name = :name")
     //Optional<City> findByName(@Param("name") String name);
    //
     //        // Опционально: поиск города по координатам
     //        @Query("SELECT c FROM City c WHERE c.latitude = :latitude AND c.longitude = :longitude")
     //Optional<City> findByCoordinates(@Param("latitude") Double latitude, @Param("longitude") Double longitude);

    Optional<City> findByLocation(Point point);

}
