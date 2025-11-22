package com.example.sparkle.sparkle.initializer;

import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.repository.CityRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


@Component
@Profile("development")
@Slf4j
public class DataLoaderCity implements CommandLineRunner {


    private final CityRepository cityRepository;

    @Autowired
    public DataLoaderCity(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (cityRepository.count() == 0) {
            List<City> cities = new ArrayList<>();
            City city;
            GeometryFactory geometryFactory = new GeometryFactory();
            Point point;
            ClassPathResource resource = new ClassPathResource("db/ru.csv");
            try (BufferedReader br = new BufferedReader(
                    (new InputStreamReader(resource.getInputStream())))) {
                String line;
                boolean isFirstLine = true;  // Флаг для пропуска заголовка
                while ((line = br.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;  // Пропускаем первую строку
                        continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length < 3) continue; // Пропускаем некорректные строки

                    String cityName = parts[0].trim();
                    log.info("parts[1].trim() >>>>" + parts[1].trim());
                    double lng = Double.parseDouble(parts[1].trim());
                    double lat = Double.parseDouble(parts[2].trim());

                    city = new City();
                    point = geometryFactory.createPoint(new Coordinate(lat,lng));
                    city.setName(cityName);
                    city.setLocation(point);
                    cities.add(city);


                }
            } catch (Exception e) {
                log.error("Ошибка загрузки CSV: {}", e.getMessage(), e);
                throw e;
            }
            cityRepository.saveAll(cities);
            log.info("Загружено {} городов", cities.size());
        }
    }















            /*City city = new City();

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
            cityRepository.save(city);*/
    //}

}


