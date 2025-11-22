package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.ToString;

import java.util.Arrays;


public enum Roles {
    ADMIN ("admin"),
    USER ("user"),
    MODERATOR("moderator");

    private final String roles;

    Roles(String roles) {
        this.roles = roles;
    }

    @JsonValue
    public String getRole() {
        return roles;
    }

    @JsonCreator
    public static Roles fromString(String roles) {
        return Arrays.stream(Roles.values())
                .filter(r -> r.roles.equals(roles))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неверная роль: " + roles));
    }

}
