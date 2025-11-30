package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Перечисление интересов пользователей с локализованными метками.
 */
@Getter
public enum Interest {
    FOOTBALL("Футбол"),
    LITRBALL("Пьянство"),
    BASKETBALL("Баскетбол"),
    TENNIS("Теннис"),
    SWIMMING("Плавание"),
    GYM("Фитнес и спортзал"),
    PAINTING("Рисование"),
    MUSIC("Музыка"),
    DANCE("Танцы"),
    WRITING("Писательство"),
    COOKING("Кулинария"),
    PHOTOGRAPHY("Фотография"),
    READING("Чтение"),
    TRAVEL("Путешествия"),
    PROGRAMMING("Программирование"),
    LANGUAGES("Изучение языков"),
    SCIENCE("Наука и технологии"),
    BUSINESS("Бизнес и предпринимательство"),
    MOVIES("Кино"),
    GAMING("Видеоигры"),
    SOCIAL_MEDIA("Социальные сети"),
    OTHER("Другое");

    private final String label;

    Interest(String label) {
        this.label = label;
    }

    /**
     * Сериализация в JSON
     */
    //@JsonValue
    public String getLabel() {
        return label;
    }
    public static List<Interest> getRandomInterest() {
        if (Math.random() > 0.5) {
            return List.of(Interest.FOOTBALL, Interest.LITRBALL, Interest.BASKETBALL);
        } else {
            return List.of(Interest.TENNIS, Interest.SWIMMING, Interest.GYM);
        }
    }
}