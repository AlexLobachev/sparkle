package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.Photo;
import com.example.sparkle.sparkle.service.PhotoService;
import com.example.sparkle.sparkle.service.UserPhotoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> uploadUserPhoto(
            @RequestParam("file") @Valid MultipartFile file
            ) throws IOException {
        try {
            Photo photo = photoService.uploadUserPhoto(file);
            return ResponseEntity.ok().body(photo);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Ошибка загрузки файла", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }


    /**
     * Получение фото пользователя
     */
    @GetMapping("/users/{userId}/photos/{photoId}")
    public ResponseEntity<?> getPhotoByIdUser(@PathVariable Long userId, @PathVariable Long photoId) throws IOException {
        Photo photo = userPhotoService.getPhotoById(userId, photoId).getPhoto();
        return ResponseEntity.ok(photo);


    }

    /**
     * Получение всех фото пользователя
     */
    @GetMapping("/all-photos/{userId}")
    public ResponseEntity<?> getAllPhotoByIdUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userPhotoService.getAllPhotoByIdUser(userId));


    }

    /**
     * Удаление фотографии пользователя
     */
    @DeleteMapping("/remove-photo/photos/{photoId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> removeUserPhoto(@PathVariable Long photoId) throws IOException {
        photoService.removeUserPhoto(photoId);
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<?> photoBuilder(Photo photo) throws IOException {
        try {
            Path resource = Paths.get(photo.getUrl(), photo.getFileName());

            // Проверка существования файла
            if (!Files.exists(resource)) {
                throw new NotFound("Фото не найдено");
            }

            byte[] imageData = Files.readAllBytes(resource);

            // Определение типа контента по расширению
            String fileExtension = photo.getFileName()
                    .substring(photo.getFileName().lastIndexOf(".") + 1)
                    .toLowerCase();
            MediaType mediaType = MediaType.parseMediaType("image/" + fileExtension);


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(imageData.length);

            return ResponseEntity.ok().headers(headers).body(imageData);


        } catch (IOException e) {
            log.error("Ошибка чтения файла: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Файл не найден: " + e.getMessage());
        } catch (Exception e) {
            log.error("Неожиданная ошибка: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка сервера: " + e.getMessage());
        }

    }


}
