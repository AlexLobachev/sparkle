package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Перечисление провайдеров аутентификации.
 * Поддерживает сериализацию/десериализацию через Jackson.
 */
@Getter
public enum AuthProvider {
    GITHUB("github"),
    GOOGLE("google"),
    VKONTAKTE("vkontakte"),
    VK("vk");

    private final String provider;

    AuthProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Сериализация в JSON
     */
    @JsonValue
    public String getProvider() {
        return provider;
    }

    /**
     * Десериализация из строки
     */
    @JsonCreator
    public static AuthProvider fromString(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("Провайдер не может быть null или пустым");
        }
        return Arrays.stream(values())
                .filter(p -> p.provider.equalsIgnoreCase(provider.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Неверный провайдер: " + provider + ". Допустимые значения: " +
                                Arrays.stream(values())
                                        .map(AuthProvider::getProvider)
                                        .collect(Collectors.joining(", "))
                ));
    }
}