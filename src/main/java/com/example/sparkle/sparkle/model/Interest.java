package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;


public enum Interest {

    // Спортивные интересы
    FOOTBALL("Футбол"),
    BASKETBALL("Баскетбол"),
    TENNIS("Теннис"),
    SWIMMING("Плавание"),
    GYM("Фитнес и спортзал"),

    // Творческие интересы
    PAINTING("Рисование"),
    MUSIC("Музыка"),
    DANCE("Танцы"),
    WRITING("Писательство"),

    // Хобби и увлечения
    COOKING("Кулинария"),
    PHOTOGRAPHY("Фотография"),
    READING("Чтение"),
    TRAVEL("Путешествия"),

    // Образование и развитие
    PROGRAMMING("Программирование"),
    LANGUAGES("Изучение языков"),
    SCIENCE("Наука и технологии"),
    BUSINESS("Бизнес и предпринимательство"),

    // Развлечения
    MOVIES("Кино"),
    GAMING("Видеоигры"),
    SOCIAL_MEDIA("Социальные сети"),

    // Другое
    OTHER("Другое");

    private final String interestName;

    Interest(String interestName) {
        this.interestName = interestName;
    }

    @JsonValue
    public String getInterestName() {
        return interestName;
    }

    @JsonCreator
    public static Interest fromString(String interestName) {
        return Arrays.stream(Interest.values())
                .filter(i -> i.interestName.equals(interestName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неверный интерес: " + interestName));
    }

    //@JsonCreator
    //public static Interest fromString(String interestName) {
    //    return Arrays.stream(Interest.values())
    //            .filter(i -> i.interestName.equals(interestName))
    //            .findFirst()
    //            .orElseThrow(() -> new IllegalArgumentException("Неверный интерес: " + interestName));
    //}
//
    //// Метод для получения всех значений в виде списка строк
    //public static List<String> getAllInterests() {
    //    return Arrays.stream(Interest.values())
    //            .map(Interest::getInterestName)
    //            .collect(Collectors.toList());
    //}
}
