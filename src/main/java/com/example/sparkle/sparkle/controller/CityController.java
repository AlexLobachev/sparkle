package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.LocationRequestDto;
import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.service.CityService;
import com.example.sparkle.sparkle.service.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sparkle/city")
public class CityController {
    private final GeocodingService geocodingService;
    private final CityService cityService;

    @Autowired
    public CityController(GeocodingService geocodingService, CityService cityService) {
        this.geocodingService = geocodingService;
        this.cityService = cityService;
    }

    @PostMapping("/location")
    public ResponseEntity<?> saveUserLocation(@RequestBody LocationRequestDto location) {
        // Получаем город по координатам
        City city = geocodingService.getCityByCoordinates(location.getLatitude(), location.getLongitude());

        if (city == null) {
            return ResponseEntity.badRequest().body("Не удалось определить город");
        }

        // Сохраняем город в БД
        cityService.saveCity(city);

        return ResponseEntity.ok().build();
    }
}
