package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.city.CityDtoName;
import com.example.sparkle.sparkle.model.City;

import java.util.List;
import java.util.Optional;

public interface CityService {


    City saveCity(City city);

    Optional<City> getCityByName(String name);

    Optional<City> getCityByCoordinates(Double latitude, Double longitude);


    List<CityDtoName> getAllCityFromDataBase();
}
