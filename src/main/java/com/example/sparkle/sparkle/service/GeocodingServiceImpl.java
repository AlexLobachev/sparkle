package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.repository.CityRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class GeocodingServiceImpl implements GeocodingService {
    private static final String GOOGLE_MAPS_API_KEY = "ВАШ_API_КЛЮЧ";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json";


    private final CityRepository cityRepository;


    private final Gson gson;

    @Autowired
    public GeocodingServiceImpl(CityRepository cityRepository, Gson gson) {
        this.cityRepository = cityRepository;
        this.gson = gson;
    }

    public City getCityByCoordinates(Double latitude, Double longitude) {
        /*try {
            String url = BASE_URL +
                    "?latlng=" + latitude + "," + longitude +
                    "&key=" + GOOGLE_MAPS_API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            String responseBody = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            // Используем Gson для парсинга
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            JsonArray results = response.getAsJsonArray("results");

            if (results.isJsonNull() || results.size() == 0) {
                return null;
            }

            String cityName = getCityNameFromAddressComponents(results.get(0).getAsJsonObject());

            Optional<City> existingCity = cityRepository.findByName(cityName);

            if (existingCity.isPresent()) {
                return existingCity.get();
            }

            City newCity = new City();
            newCity.setName(cityName);
            newCity.setLatitude(latitude);
            newCity.setLongitude(longitude);

            return newCity;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении геолокации", e);
        }*/
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(longitude,latitude));
        point.setSRID(4326);
        return cityRepository.findByLocation(point).orElseThrow();

        //return cityRepository.findByCoordinates(point).orElseThrow();
    }
    @Override
    public City getCityByName(String name) {
        if (name == null) {
            return null;
        } else {
            return cityRepository.findByName(name).orElseThrow(()-> new NotFound("Город не найден"));
        }
    }

    private String getCityNameFromAddressComponents(JsonObject result) {
        JsonArray addressComponents = result.getAsJsonArray("address_components");

        for (JsonElement component : addressComponents) {
            JsonObject addressComponent = component.getAsJsonObject();
            JsonArray types = addressComponent.getAsJsonArray("types");

            if (types.size() > 0 && types.get(0).getAsString().equals("locality")) {
                return addressComponent.getAsJsonObject("long_name").getAsString();
            }
        }

        return null;
    }
}
