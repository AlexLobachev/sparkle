package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;


public enum AuthProvider {
    GITHUB ("github"),
    GOOGLE ("google"),
    VKONTAKTE ("vkontakte"),
    VK ("vk");

    private final String provider;

    AuthProvider(String provider) {
        this.provider = provider;
    }

    @JsonValue
    public String getProvider() {
        return provider;
    }

    @JsonCreator
    public static AuthProvider fromString(String provider) {
        return Arrays.stream(AuthProvider.values())
                .filter(p -> p.provider.equals(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неверный провайдер: " + provider));
    }
}
