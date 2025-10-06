package com.example.sparkle.sparkle.model;

public enum Gender {
    WOMEN ("Женский"),
    MAN ("Мужской");

    private final String gender;

    Gender(String gender) {
        this.gender = gender;
    }

    public String toString(Gender gen) {
        return gender;
    }
}
