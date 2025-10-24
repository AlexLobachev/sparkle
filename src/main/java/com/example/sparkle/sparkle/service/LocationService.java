package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.User;

import java.util.List;

public interface LocationService {
    /**
     *Метод для расчета расстояния между двумя точками/
     */
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);
    /**
     Метод для поиска пользователей по близости местоположения
     */

    List<User> findUsersNearby(double lat, double lon, double radius);

    /**
     * Метод для обновления местоположения пользователя
     * */
    void updateUserLocation(Long userId, double lat, double lon);
}
