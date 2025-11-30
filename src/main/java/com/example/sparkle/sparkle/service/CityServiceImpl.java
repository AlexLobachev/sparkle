package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.city.CityDtoLocation;
import com.example.sparkle.sparkle.dto.city.CityDtoName;
import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.PositionstackResponse;
import com.example.sparkle.sparkle.repository.CityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class CityServiceImpl implements CityService {


    private final CityRepository cityRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory();



    @Value("${positionstack.api.key}")
    private String positionstackKey;

    @Value("${positionstack.api.url}")
    private String positionstackUrl;

    @Autowired
    public CityServiceImpl(CityRepository cityRepository,
                           RestTemplate restTemplate,
                           ObjectMapper objectMapper) {
        this.cityRepository = cityRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public CityDtoLocation saveUserLocation(CityDtoLocation dto) {
        if (dto.getLatitude() == null || dto.getLongitude() == null) {
            throw new IllegalArgumentException("Координаты (lat, lng) обязательны");
        }

        // 1. Геокодируем: координаты → город
        PositionstackResponse.Data cityData = reverseGeocode(dto.getLatitude(), dto.getLongitude());
        if (cityData == null) {
            throw new IllegalStateException("Не удалось определить город по координатам");
        }

        String cityName = cityData.locality() != null ? cityData.locality() : cityData.name();
        if (cityName == null) cityName = "Неизвестный город";

        // 2. Проверяем, есть ли уже такой город
        Optional<City> existing = cityRepository.findByNameIgnoreCase(cityName);
        if (existing.isPresent()) {
            return CityDtoLocation.toCityDtoLocation(existing.get());
        }

        // 3. Создаём новый город
        Point location = createPoint(cityData.longitude(), cityData.latitude());
        City city = new City();
        city.setName(cityName);
        city.setLocation(location);
        city.setSource("gps");
        city.setAccuracy(dto.getAccuracy());

        return CityDtoLocation.toCityDtoLocation(cityRepository.save(city));
    }


    private PositionstackResponse.Data reverseGeocode(double lat, double lng) {
        try {
            String url = positionstackUrl + "/reverse" +
                    "?access_key=" + positionstackKey +
                    "&query=" + lat + "," + lng +
                    "&limit=1";

            String response = restTemplate.getForObject(url, String.class);
            PositionstackResponse psResponse = objectMapper.readValue(response, PositionstackResponse.class);

            if (psResponse.data() != null && !psResponse.data().isEmpty()) {
                return psResponse.data().get(0);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при геокодировании: " + e.getMessage(), e);
        }
    }

    private Point createPoint(Double lng, Double lat) {
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        point.setSRID(4326);
        return point;
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

    @Override
    public List<CityDtoName> getAllCityFromDataBase() {
        // Получаем город по координатам
        List<City> cities = cityRepository.findAll();
        return cities.stream().map(CityDtoName::toCityDtoName).toList();
    }


    /**
     * Обратное геокодирование: координаты → название города
     */
    /*private String reverseGeocode(double lat, double lng) {
        try {
            String encodedUrl = "https://maps.googleapis.com/maps/api/geocode/json" +
                    "?latlng=" + lat + "," + lng +
                    "&key=ВАШ_API_КЛЮЧ" +
                    "&language=ru";

            String response = restTemplate.getForObject(encodedUrl, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");


            if (results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.get(0);
                JsonNode addressComponents = firstResult.path("address_components");


                for (JsonNode component : addressComponents) {
                    JsonNode types = component.path("types");
                    if (hasType(types, "locality") || hasType(types, "administrative_area_level_1")) {
                        return component.path("long_name").asText();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private boolean hasType(JsonNode typesArray, String targetType) {
        if (typesArray.isArray()) {
            for (JsonNode element : typesArray) {
                if (element.asText().equals(targetType)) {
                    return true;
                }
            }
        }
        return false;
    }*/

    /**
     * Прямое геокодирование: название города → координаты
     */
    /*
    private Point geocode(String cityName) {
        try {
            String encodedName = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
            String url = "https://maps.googleapis.com/maps/api/geocode/json" +
                    "?address=" + encodedName +
                    "&key=ВАШ_API_КЛЮЧ" +
                    "&language=ru";

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");

            if (results.isArray() && results.size() > 0) {
                JsonNode location = results.get(0).path("geometry").path("location");
                double lat = location.path("lat").asDouble();
                double lng = location.path("lng").asDouble();

                return createPoint(lng, lat);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/
}
