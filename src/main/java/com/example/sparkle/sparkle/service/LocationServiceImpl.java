package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    private final UserRepository userRepository;

    @Autowired
    public LocationServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
    *Метод для расчета расстояния между двумя точками/
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Формула для расчета расстояния между двумя точками на Земле
        double earthRadius = 6371; // Радиус Земли в километрах
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
/**
Метод для поиска пользователей по близости местоположения
 */

    public List<User> findUsersNearby(double lat, double lon, double radius) {
        List<User> users = userRepository.findAll();
        List<User> nearbyUsers = new ArrayList<>();
        for (User user : users) {
            double distance = calculateDistance(lat, lon, user.getCity().getLatitude(), user.getCity().getLongitude());
            if (distance <= radius) {
                nearbyUsers.add(user);
            }
        }
        return nearbyUsers;
    }

    /**
     * Метод для обновления местоположения пользователя
     * */
    public void updateUserLocation(Long userId, double lat, double lon) {
        //User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        User user = userRepository.findById(userId).get();
        user.getCity().setLatitude(lat);
        user.getCity().setLongitude(lon);
        userRepository.save(user);
    }
}
