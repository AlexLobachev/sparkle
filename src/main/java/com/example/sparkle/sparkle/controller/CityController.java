package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.LocationRequestDto;
import com.example.sparkle.sparkle.dto.city.CityDtoLocation;
import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.service.CityService;
import com.example.sparkle.sparkle.service.CityServiceImpl;
import com.example.sparkle.sparkle.service.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sparkle/city")
public class CityController {

    private final CityService cityService;

    @Autowired
    public CityController(
                          CityService cityService) {
        this.cityService = cityService;
    }



    @PostMapping("/location")
    public ResponseEntity<CityDtoLocation> saveUserLocation(@RequestBody CityDtoLocation location) {
        return ResponseEntity.ok(cityService.saveUserLocation(location));
    }

    @GetMapping("/get-all-cities")
    public ResponseEntity<?> getAllCityFromDataBase() {
        return ResponseEntity.ok(cityService.getAllCityFromDataBase());
    }
}
