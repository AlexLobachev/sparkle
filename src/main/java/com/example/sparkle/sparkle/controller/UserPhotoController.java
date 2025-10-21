package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.builder.BuilderPhoto;
import com.example.sparkle.sparkle.model.Photo;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserPhoto;
import com.example.sparkle.sparkle.service.PhotoService;
import com.example.sparkle.sparkle.service.UserPhotoService;
import com.example.sparkle.sparkle.service.UserService;
import com.example.sparkle.sparkle.validator.ValidatorUser;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sparkle/users/photo")
@Slf4j
public class UserPhotoController {

    private final PhotoService photoService;
    private final UserPhotoService userPhotoService;
    private final UserService userService;

    @Autowired
    public UserPhotoController(PhotoService photoService, UserPhotoService userPhotoService, UserService userService) {
        this.photoService = photoService;
        this.userPhotoService = userPhotoService;
        this.userService = userService;
    }

    /**
     * Загрузка фотографии пользователя
     */
    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadUserPhoto(
            @RequestParam("file") @Valid MultipartFile file,
            @RequestParam("userId") Long userId) throws IOException {
        Optional<User> user = userService.getUserById(userId);
        ResponseEntity<?> validationResult = ValidatorUser.userNotFoundAndForbidden(
                user.orElse(null),
                userId);

        if (!validationResult.getStatusCode().is2xxSuccessful()) {
            return validationResult;
        }
        try {
            if (file.getSize() > 5242880) { // 5MB
                throw new FileSizeLimitExceededException("Размер файла превышает допустимый", file.getSize(), 5242880);
            }
            log.info("Начата загрузка фото");
            Photo photo;
            ResponseEntity.ok(photo = photoService.uploadUserPhoto(file, userId)).getBody();
            log.info("Загрузка фото - успех");
            return BuilderPhoto.photoBuilder(photo);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Ошибка загрузки файла", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }


    }

    /**
     * Получение фото пользователя
     */
    @GetMapping("/users/{userId}/photos/{photoId}")
    public ResponseEntity<?> getPhotoByIdUser(@PathVariable Long userId, @PathVariable Long photoId) throws IOException {
        Optional<User> user = userService.getUserById(userId);
        ResponseEntity<?> validationResult = ValidatorUser.userNotFoundAndForbidden(
                user.orElse(null),
                userId);

        if (!validationResult.getStatusCode().is2xxSuccessful()) {
            return validationResult;
        }
        try {
            UserPhoto userPhoto = userPhotoService.getPhotoById(photoId);
            if (userPhoto == null) {
                log.debug("Фото не найдено");
                return ResponseEntity.noContent().build();
            } else if (!userPhoto.getUser().getId().equals(userId)) {
                log.debug("Фото не принадлежит пользователю с ID = {}", userId);
                return new ResponseEntity<>("Доступ запрещен", HttpStatus.FORBIDDEN);
            } else {
                Photo photo = userPhoto.getPhoto();
                return BuilderPhoto.photoBuilder(photo);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Ошибка загрузки фото", HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Получение всех фото пользователя
     */
    @GetMapping("/all-photos/{userId}")
    public ResponseEntity<?> getAllPhotoByIdUser(@PathVariable Long userId) {
        Optional<User> user = userService.getUserById(userId);
        ResponseEntity<?> validationResult = ValidatorUser.userNotFoundAndForbidden(
                user.orElse(null),
                userId);

        if (!validationResult.getStatusCode().is2xxSuccessful()) {
            return validationResult;
        }
        try {
            List<UserPhoto> userPhotoList = userPhotoService.getAllPhotoByIdUser(userId);
            if (userPhotoList.isEmpty()) {
                log.info("Пользователь еще не загрузил ни фото");
                return ResponseEntity.noContent().build();

            } else if (userPhotoList.stream().noneMatch(userPhoto -> userPhoto.getUser().getId().equals(userId))) {
                log.info("Фото не принадлежит пользователю с ID = {}", userId);
                return new ResponseEntity<>("Доступ запрещен",HttpStatus.FORBIDDEN);
            } else {
                return ResponseEntity.ok(userPhotoList.stream().map(userPhoto -> {
                    try {
                        return BuilderPhoto.photoBuilder(userPhoto.getPhoto());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Ошибка загрузки фото", HttpStatus.NOT_FOUND);
        }


    }

    /**
     * Удаление фотографии пользователя
     */
    @DeleteMapping("/remove-photo/users/{userId}/photos/{photoId}")
    public ResponseEntity<?> removeUserPhoto(@PathVariable Long userId, @PathVariable Long photoId) throws IOException {
        Optional<User> user = userService.getUserById(userId);
        ResponseEntity<?> validationResult = ValidatorUser.userNotFoundAndForbidden(
                user.orElse(null),
                userId);

        if (!validationResult.getStatusCode().is2xxSuccessful()) {
            return validationResult;
        }
        List<UserPhoto> userPhotoList = userPhotoService.getAllPhotoByIdUser(userId);

        Optional<UserPhoto> optionalPhoto = userPhotoList.stream()
                .filter(photo -> photo.getId().equals(photoId))
                .findFirst();

        if (optionalPhoto.isPresent()) {
            Photo userPhoto = optionalPhoto.get().getPhoto();
            // работаем с найденным фото
            photoService.removeUserPhoto(userPhoto);
            ResponseEntity.ok();
        } else {
            log.info("Фото не найдено");
            ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }


}
