package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Gender {
    WOMEN("Женский"),
    MAN("Мужской");

    private final String gender;

    Gender(String gender) {
        this.gender = gender;
    }

    @JsonValue
    public String getGender() {
        return gender;
    }

    @JsonCreator
    public static Gender fromString(String gender) {
        return Arrays.stream(Gender.values())
                .filter(g -> g.gender.equals(gender))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неверный пол: " + gender));
    }
}



