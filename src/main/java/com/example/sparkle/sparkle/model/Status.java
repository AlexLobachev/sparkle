package com.example.sparkle.sparkle.model;

/**
 * Статус пользователя в системе.
 * DRAFT — незавершённый профиль; COMPLETE — профиль заполнен.
 */
public enum Status {
    DRAFT,      // Создан, но профиль не заполнен
    COMPLETE    // Профиль заполнен полностью
}