package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO для ответа Positionstack API при обратном геокодировании.
 * Пример: /reverse?query=55.75,37.62
 */
public record PositionstackResponse(
        @JsonProperty("data") List<Data> data
) {
    public record Data(
            @JsonProperty("latitude") Double latitude,
            @JsonProperty("longitude") Double longitude,
            @JsonProperty("name") String name,
            @JsonProperty("locality") String locality,
            @JsonProperty("administrative_area") String administrativeArea,
            @JsonProperty("country") String country,
            @JsonProperty("label") String label
    ) {}
}