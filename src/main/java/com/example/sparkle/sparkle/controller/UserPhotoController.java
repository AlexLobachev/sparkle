package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.model.UserPhoto;
import com.example.sparkle.sparkle.service.UserPhotoService;
import com.example.sparkle.sparkle.service.UserPhotoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserPhotoController {

    private final UserPhotoService photoService;

    public UserPhotoController(UserPhotoService photoService) {
        this.photoService = photoService;
    }

    /**
     * Загрузка фотографии пользователя
     */
    @PostMapping("/upload-photo/{userId}")
    public ResponseEntity<?> uploadUserPhoto(@RequestParam("file") MultipartFile file, @PathVariable Long userId) throws IOException {
        try {

            return ResponseEntity.ok(photoService.uploadUserPhoto(file, userId));
        } catch (RuntimeException e) {
            new ResponseEntity<>("Ошибка загрузки файла", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            return null;
        }


    }

    /**
     * Удаление фотографии пользователя
     */
    @DeleteMapping("/remove-photo/{photoId}")
    public ResponseEntity<Void> removeUserPhoto(@PathVariable Long photoId) {
        photoService.removeUserPhoto(photoId);
        return ResponseEntity.noContent().build();
    }


}
