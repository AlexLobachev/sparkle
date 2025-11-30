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
            GeometryFactory geometryFactory = new GeometryFactory();
            ClassPathResource resource = new ClassPathResource("db/new_cities.csv");

            int lineNumber;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream()))) {

                String line;
                lineNumber = 0;
                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    if (lineNumber == 1) continue; // Пропускаем заголовок

                    String[] parts = line.split(",");
                    if (parts.length < 19) { // Проверяем наличие всех необходимых полей
                        log.warn("Строка {} пропущена (недостаточно полей): {}", lineNumber, line);
                        continue;
                    }

                    try {
                        String cityName = parts[6].trim(); // Город
                        double lat = Double.parseDouble(parts[17].trim().replace(',', '.')); // Широта
                        double lng = Double.parseDouble(parts[18].trim().replace(',', '.')); // Долгота

                        if (lng < -180 || lng > 180 || lat < -90 || lat > 90) {
                            log.warn("Строка {}: некорректные координаты для {}: {}, {}",
                                    lineNumber, cityName, lat, lng);
                            continue;
                        }

                        City city = new City();
                        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
                        city.setName(cityName);
                        city.setLocation(point);
                        cities.add(city);

                    } catch (NumberFormatException e) {
                        log.warn("Строка {}: ошибка парсинга координат в '{}'", lineNumber, line, e);
                    }
                }
            } catch (Exception e) {
                log.error("Ошибка загрузки CSV: {}", e.getMessage(), e);
                throw e;
            }

            cityRepository.saveAll(cities);
            log.info("Загружено {} городов из {} строк CSV", cities.size(), lineNumber - 1);
        }
    }



}


