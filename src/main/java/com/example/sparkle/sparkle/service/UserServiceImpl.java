package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.LocationRequestDto;
import com.example.sparkle.sparkle.dto.user.UserDto;
import com.example.sparkle.sparkle.dto.user.UserDtoUpdate;
import com.example.sparkle.sparkle.dto.user.UserMapper;
import com.example.sparkle.sparkle.exception.BadRequest;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.AuthProvider;
import com.example.sparkle.sparkle.model.City;
import com.example.sparkle.sparkle.model.Status;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.repository.UserRepository;
import com.example.sparkle.sparkle.repository.UserRolesRepository;
import com.example.sparkle.sparkle.validator.ValidatorUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Класс для работы с пользователями
 */
@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ValidatorUser validatorUser;
    private final GeocodingService geocodingService;
    private final UserRolesRepository userRolesRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           ValidatorUser validatorUser,
                           GeocodingService geocodingService,
                           UserRolesRepository userRolesRepository) {
        this.userRepository = userRepository;
        this.validatorUser = validatorUser;
        this.geocodingService = geocodingService;
        this.userRolesRepository = userRolesRepository;
    }


    /**
     * Регистрация нового пользователя (вручную через Email) (Не поддерживается)
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
     * Регистрация нового пользователя через соц. сеть
     */
    @Override
    @Transactional
    public Optional<User> registerUserBySocialNetwork(User user) {
        //validatorUser.userNotFound(user);
        try {
            user = userRepository.save(user);
            userRolesRepository.save(user.getRoles().stream().findFirst().orElseThrow(() -> new NotFound("Роли не заданы")));
            return Optional.of(user);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new BadRequest(e.getMessage());
        }


    }

    /**
     * Редактирование профиля пользователя
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public Optional<UserDtoUpdate> updateUserProfile(UserDtoUpdate userDtoUpdate) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User currentUserEntity = getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
        userDtoUpdate.setId(currentUserEntity.getId());

        userDtoUpdate = validatorUser.invalidRequest(currentUserEntity, userDtoUpdate);

        if (userDtoUpdate.getCity() == null) {
            if (currentUserEntity.getCity() != null) {
                userDtoUpdate.setCity(currentUserEntity.getCity().getName());
            } else {
                throw new BadRequest("Город не указан");
            }

        }
        City city = geocodingService.getCityByName(userDtoUpdate.getCity());
        int affectedRows =
                userRepository.userUpdate(
                        userDtoUpdate.getGender().toString(),
                        userDtoUpdate.getPreferredGender().toString(),
                        userDtoUpdate.getEmail(),
                        userDtoUpdate.getBirthDate(),
                        userDtoUpdate.getAboutMe(),
                        userDtoUpdate.getId(),
                        city.getId());

        entityManager.refresh(userRepository.findById(userDtoUpdate.getId()).orElseThrow());

        if (affectedRows == 0) {
            throw new NotFound("Пользователь не найден");
        }
        log.info("Обновление пользователя с ID = {} прошло успешно", userDtoUpdate.getId());
        if (userDtoUpdate.getEmail() != null)
            userRepository.updateEmailStatus(userDtoUpdate.getId());
        currentUserEntity = userRepository.findById(currentUserEntity.getId()).orElse(null);
        validatorUser.userNotFound(currentUserEntity);
        return Optional.of(UserMapper.toUserDtoUpdate(Objects.requireNonNull(currentUserEntity)));
    }

    /**
     * Ввод параметров при регистрации пользователя (интересы, пол и пр.)
     */

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @Override
    public Optional<User> setupUserProfile(UserDtoUpdate userDtoUpdate) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userRepository.findByUsername(
                currentUser.getUsername()).orElseThrow(() -> new NotFound("Пользователь не найден"));
        validatorUser.userForbidden(getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден")), user.getId());
        userDtoUpdate.setId(user.getId());
        if (userDtoUpdate.getCity() == null) {
            throw new BadRequest("Город не указан");
        }
        City city = geocodingService.getCityByName(userDtoUpdate.getCity());

        int affectedRows =
                userRepository.userUpdate(
                        userDtoUpdate.getGender().toString(),
                        userDtoUpdate.getPreferredGender().toString(),
                        userDtoUpdate.getEmail(),
                        userDtoUpdate.getBirthDate(),
                        userDtoUpdate.getAboutMe(),
                        userDtoUpdate.getId(),
                        city.getId());
        entityManager.refresh(userRepository.findById(user.getId()).orElseThrow());

        if (affectedRows == 0) {
            log.warn("Обновление пользователя {} не выполнено - запись не найдена", currentUser.getUsername());
            throw new NotFound("Пользователь не найден");
        }
        user.setStatus(Status.COMPLETE);
        user = userRepository.findById(user.getId()).orElseThrow(() -> new NotFound("Пользователь не найден"));

        log.info("Обновление пользователя с ID = {} прошло успешно", user.getId());
        if (user.getEmail() != null)
            userRepository.updateEmailStatus(user.getId());
        return Optional.of(user);
    }


    /**
     * Получение профиля пользователя по id
     */
    @Override
    public Optional<UserDto> getUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        validatorUser.userNotFound(user);
        validatorUser.userForbidden(user, userId);
        return Optional.of(UserDto.toUserDto(user));
    }

    /**
     * Получение пользователя по ID провайдера
     */
    @Override
    public Optional<User> getUserByExternalId(String externalId) {
        User user = userRepository.findByExternalId(externalId).orElseThrow(() -> new NotFound("Пользователь не найден"));
        validatorUser.userNotFound(user);
        return Optional.of(user);
    }

    /**
     * Получение пользователя по ID и провайдеру
     */
    @Override
    public Optional<User> findByExternalIdAndProvider(String externalId, String provider) {
        User user = userRepository.findByExternalIdAndProvider(externalId, AuthProvider.fromString(provider)).orElse(null);

        return Optional.ofNullable(user);
    }


    /**
     * Получение списка всех пользователей
     */
    @Override
    public List<UserDto> getUserAll() {
        Sort sort = Sort.by("id").ascending();
        List<UserDto> usersDto = userRepository.findAll(sort)
                .stream()
                .map(UserDto::toUserDto).toList();
        validatorUser.userNoContent(usersDto);
        return usersDto;
    }

    /**
     * Удаление пользователя по ID
     */
    @Override
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
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
    public Optional<User> saveUserLocation(LocationRequestDto location, @PathVariable Long userId) {
        // Получаем текущего пользователя
        //User user = userService.getUserByUserName(userDetails.getUsername());
        User user = userRepository.findById(userId).orElseThrow();

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
