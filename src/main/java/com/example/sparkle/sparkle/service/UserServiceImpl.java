package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.LocationRequestDto;
import com.example.sparkle.sparkle.dto.user.UserDtoUpdate;
import com.example.sparkle.sparkle.exception.BadRequest;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.repository.UserRepository;
import com.example.sparkle.sparkle.validator.ValidatorUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;
/**
 * Класс для работы с пользователями
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ValidatorUser validatorUser;
    private final GeocodingService geocodingService;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ValidatorUser validatorUser,GeocodingService geocodingService) {
        this.userRepository = userRepository;
        this.validatorUser = validatorUser;
        this.geocodingService = geocodingService;
    }


    /**
     * Регистрация нового пользователя
     * Пользователь вводит Имя, Пол, Дату рождения
     */
    @Override
    public Optional<User> registerUser(User user) {
        validatorUser.userNotFound(user);
        validatorUser.userBadRequestEmail(user);
        Optional<User> userValid = userRepository.findByEmail(user.getEmail());
        validatorUser.userConflictEmail(user, userValid.orElse(null));


        return Optional.of(userRepository.save(user));
    }

    /**
     * Редактирование профиля пользователя
     */
    @Transactional
    public Optional<User> updateUserProfile(Long userId, UserDtoUpdate userDtoUpdate) {
        User user = getUserById(userId).orElseThrow();
        userDtoUpdate.setId(userId);
        validatorUser.invalidRequest(user, userDtoUpdate);

        if (userDtoUpdate.getGender() == null) userDtoUpdate.setGender(user.getGender());
        if (userDtoUpdate.getPreferredGender() == null) userDtoUpdate.setPreferredGender(user.getPreferredGender());

        int affectedRows =
                userRepository.userUpdate(
                        userDtoUpdate.getUsername(),
                        userDtoUpdate.getGender().toString(),
                        userDtoUpdate.getPreferredGender().toString(),
                        userDtoUpdate.getEmail(),
                        userDtoUpdate.getBirthDate(),
                        userDtoUpdate.getAboutMe(),
                        userDtoUpdate.getId());
        entityManager.refresh(userRepository.findById(userDtoUpdate.getId()).orElseThrow());

        if (affectedRows == 0) {
            log.warn("Обновление пользователя с ID = {} не выполнено - запись не найдена", userId);
            throw new NotFound("Пользователь не найден");
        }
        log.info("Обновление пользователя с ID = {} прошло успешно", userDtoUpdate.getId());
        user = getUserById(userId).orElse(null);
        validatorUser.userNotFound(user);
        return Optional.ofNullable(user);
    }


    /**
     * Получение профиля пользователя по id
     */
    @Override
    public Optional<User> getUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        validatorUser.userNotFound(user);
        validatorUser.userForbidden(user, userId);
        return Optional.of(user);
    }

    /**
     * Получение списка всех пользователей
     */
    @Override
    public List<User> getUserAll() {
        Sort sort = Sort.by("id").ascending();
        List<User> users = userRepository.findAll(sort);
        validatorUser.userNoContent(users);
        return users;
    }

    /**
     * Удаление пользователя по ID
     */
    @Override
    public void deleteUserById(Long userId) {
        User user = getUserById(userId).orElse(null);
        assert user != null;
        userRepository.deleteById(user.getId());
        user = userRepository.findById(userId).orElse(null);
        validatorUser.userConflictDelete(user);
    }

    /**
     * Получение пользователя по имени
     */
    @Override
    public Optional<User> getUserByUserName(String name) {
        User user = userRepository.findByUsername(name).orElse(null);
        validatorUser.userNotFound(user);
        return Optional.ofNullable(user);
    }
    /**
     * Сохранение локации пользователя
     */
    @Transactional
    public Optional<User> saveUserLocation(LocationRequestDto location, @PathVariable Long userId){
        // Получаем текущего пользователя
        //User user = userService.getUserByUserName(userDetails.getUsername());
        User user = getUserById(userId).orElseThrow();

        // Получаем город по координатам
        City city = geocodingService.getCityByCoordinates(location.getLatitude(), location.getLongitude());

        if (city == null) {
            throw new BadRequest("Не удалось определить город");
        }

        // Связываем пользователя с городом
        user.setCity(city);
        return Optional.of(userRepository.save(user));
    }

}
