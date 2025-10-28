package com.example.sparkle.sparkle.service.test.city;

import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.repository.CityRepository;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Component
@Profile("development")
@Slf4j
public class TestDataLoader implements CommandLineRunner {


    private final CityRepository cityRepository;

    @Autowired
    public TestDataLoader(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (cityRepository.count() == 0) {
            City city = new City();

            GeometryFactory geometryFactory = new GeometryFactory();

            Point point = geometryFactory.createPoint(new Coordinate( 37.6173,55.7558));
            city.setName("Москва");
            city.setLocation(point);
            cityRepository.save(city);

            city = new City();
            point = geometryFactory.createPoint(new Coordinate( 37.2777,55.678));
            city.setName("Одинцово");
            city.setLocation(point);
            cityRepository.save(city);

            city = new City();
            point = geometryFactory.createPoint(new Coordinate( 36.73,55.39));
            city.setName("Наро-Фоминск");
            city.setLocation(point);
            cityRepository.save(city);

            city = new City();
            point = geometryFactory.createPoint(new Coordinate( 37.97,55.93));
            city.setName("Щёлково");
            city.setLocation(point);
            cityRepository.save(city);
        }

    }
}

