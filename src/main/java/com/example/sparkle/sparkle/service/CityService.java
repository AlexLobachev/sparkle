package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.city.CityDtoLocation;
import com.example.sparkle.sparkle.dto.city.CityDtoName;
import com.example.sparkle.sparkle.model.City;

import java.util.List;
import java.util.Optional;

public interface CityService {


    CityDtoLocation saveUserLocation(CityDtoLocation location);

    Optional<City> getCityByName(String name);

    Optional<City> getCityByCoordinates(Double latitude, Double longitude);


    List<CityDtoName> getAllCityFromDataBase();
}
