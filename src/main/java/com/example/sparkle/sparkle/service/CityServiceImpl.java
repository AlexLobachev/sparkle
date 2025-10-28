package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Autowired
    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    @Override
    public City saveCity(City city) {
        return cityRepository.save(city);
    }

    @Override
    public Optional<City> getCityByName(String name) {
        //return cityRepository.findByName(name);
        return null;
    }

    @Override
    public Optional<City> getCityByCoordinates(Double latitude, Double longitude) {
        //return cityRepository.findByCoordinates(latitude, longitude);
        return null;
    }
}
