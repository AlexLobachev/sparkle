package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.builder.BuilderPhoto;
import com.example.sparkle.sparkle.model.Photo;
import com.example.sparkle.sparkle.service.PhotoService;
import com.example.sparkle.sparkle.service.UserPhotoService;
import com.example.sparkle.sparkle.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Класс для добавления лайков пользователю
 */
@RestController
@RequestMapping("/sparkle/users/photo")
@Slf4j
public class UserPhotoController {

    private final PhotoService photoService;
    private final UserPhotoService userPhotoService;
    @Autowired
    public UserPhotoController(PhotoService photoService, UserPhotoService userPhotoService) {
        this.photoService = photoService;
        this.userPhotoService = userPhotoService;

    }

    /**
     * Загрузка фотографии пользователя
     */
    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadUserPhoto(
            @RequestParam("file") @Valid MultipartFile file,
            @RequestParam("userId") Long userId) throws IOException {
        try {
            Photo photo = photoService.uploadUserPhoto(file, userId);
            return ResponseEntity.ok(BuilderPhoto.photoBuilder(photo));
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Ошибка загрузки файла", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    /**
     * Получение фото пользователя
     */
    @GetMapping("/users/{userId}/photos/{photoId}")
    public ResponseEntity<?> getPhotoByIdUser(@PathVariable Long userId, @PathVariable Long photoId) throws IOException {

        return ResponseEntity.ok(BuilderPhoto.photoBuilder(userPhotoService.getPhotoById(userId, photoId).getPhoto()));


    }

    /**
     * Получение всех фото пользователя
     */
    @GetMapping("/all-photos/{userId}")
    public ResponseEntity<?> getAllPhotoByIdUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userPhotoService.getAllPhotoByIdUser(userId).stream().map(userPhoto -> {
            try {
                return BuilderPhoto.photoBuilder(userPhoto.getPhoto());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));


    }

    /**
     * Удаление фотографии пользователя
     */
    @DeleteMapping("/remove-photo/users/{userId}/photos/{photoId}")
    public ResponseEntity<?> removeUserPhoto(@PathVariable Long userId, @PathVariable Long photoId) throws IOException {
        photoService.removeUserPhoto(photoId, userId);
        return ResponseEntity.ok().build();
    }


}
