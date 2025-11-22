package com.example.sparkle.sparkle.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;


public enum Interest {
    // Спортивные интересы
    FOOTBALL("Футбол"),
    LITRBALL("Пьянство"),
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

    private final String interests;

    Interest(String interests) {
        this.interests = interests;
    }

    //@JsonValue  // ← ВАЖНО: указываем, что это поле будет в JSON
    public String getLabel() {
        return interests;
    }



}


/*


    private final String interestName;

    Interest(String interestName) {
        this.interestName = interestName;
    }
 */
