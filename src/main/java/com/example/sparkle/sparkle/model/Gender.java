package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Перечисление пола пользователя.
 * Поддерживает регистронезависимую десериализацию.
 */
public enum Gender {
    WOMEN,
    MAN;

    //@JsonValue
    public String toLower() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Gender fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Значение пола не может быть null или пустым");
        }
        try {
            return Gender.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимое значение пола: " + value + ". Допустимые: women, man");
        }
    }
}