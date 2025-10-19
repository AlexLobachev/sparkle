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

    public static Gender gender (String gen){
        if (gen.equals("Женский")){
            return Gender.WOMEN;
        }
        return Gender.MAN;
    }
}
