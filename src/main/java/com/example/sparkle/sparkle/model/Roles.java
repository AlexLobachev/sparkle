package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Перечисление ролей пользователей с поддержкой сериализации/десериализации.
 * Регистронезависимая обработка входящих значений.
 */
public enum Roles {
    ADMIN("admin"),
    USER("user"),
    MODERATOR("moderator");

    private final String role;

    Roles(String role) {
        this.role = role;
    }

    /**
     * Сериализация в JSON
     */
    @JsonValue
    public String getRole() {
        return role;
    }

    /**
     * Десериализация из строки (регистронезависимо)
     */
    @JsonCreator
    public static Roles fromString(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Роль не может быть null или пустой");
        }
        return Arrays.stream(values())
                .filter(r -> r.role.equalsIgnoreCase(role.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Неверная роль: '" + role + "'. Допустимые значения: " +
                                Arrays.stream(values())
                                        .map(Roles::getRole)
                                        .collect(Collectors.joining(", "))
                ));
    }
}