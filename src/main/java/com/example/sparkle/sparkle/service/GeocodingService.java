package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.City;

public interface GeocodingService {


    City getCityByCoordinates(Double latitude, Double longitude);

    public City getCityByName(String name);
}
