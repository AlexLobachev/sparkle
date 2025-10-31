package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.Photo;
import com.example.sparkle.sparkle.model.UserPhoto;
import com.example.sparkle.sparkle.repository.PhotoRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PhotoServiceImpl implements PhotoService {
    private final PhotoRepository photoRepository;
    private final UserService userService;
    private final UserPhotoService userPhotoService;

    @Autowired
    public PhotoServiceImpl(PhotoRepository photoRepository, UserService userService, UserPhotoService userPhotoService) {
        this.photoRepository = photoRepository;
        this.userService = userService;
        this.userPhotoService = userPhotoService;

    }


    /**
     * Загрузка фотографии пользователя
     */
    @Override
    public Photo uploadUserPhoto(MultipartFile multipartFile, Long userId) throws IOException {
        userService.getUserById(userId).orElseThrow();
        if (multipartFile.getSize() > 5242880) { // 5MB
            throw new FileSizeLimitExceededException("Размер файла превышает допустимый", multipartFile.getSize(), 5242880);
        }
        Photo photo = uploadFile(multipartFile, userId);
        assert photo != null;
        photoRepository.save(photo);
        log.info("Создаем объект для БД userPhoto");
        UserPhoto userPhoto = new UserPhoto();
        userPhoto.setUser(userService.getUserById(userId).orElseThrow());
        userPhoto.setPhoto(photo);
        log.info("Сохраняем объект для БД userPhoto");
        userPhotoService.saveUserPhoto(userPhoto);
        return photo;
    }

    /**
     * Удаление фотографии пользователя
     */
    @Override
    public void removeUserPhoto(Long photoId, Long userId) throws IOException {
        userService.getUserById(userId).orElseThrow();
        List<UserPhoto> userPhotoList = userPhotoService.getAllPhotoByIdUser(userId);

        Optional<UserPhoto> optionalPhoto = userPhotoList.stream()
                .filter(photo -> photo.getId().equals(photoId))
                .findFirst();

        if (optionalPhoto.isPresent()) {
            Photo userPhoto = optionalPhoto.get().getPhoto();
            // работаем с найденным фото
            Path photoPath = Paths.get(userPhoto.getFilePath(), userPhoto.getFileName());
            if (Files.exists(photoPath)) {
                Files.delete(photoPath);
            }
            userPhotoService.deleteByPhotoId(userPhoto.getId());
            photoRepository.deleteById(userPhoto.getId());

        } else {
            log.info("Фото не найдено");
            ResponseEntity.notFound().build();
        }


    }


    private Photo uploadFile(MultipartFile file, Long userId) throws IOException {
        log.info("Проверяем, что файл не пустой");
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }
        log.info("Создаем уникальное имя файла");
        String originalFilename = file.getOriginalFilename();
        String fileName = String.join("-", UUID.randomUUID().toString(), originalFilename);

        log.info("Создаем путь для сохранения");
        File directory = new File("C:/Users/Mi/Documents/rep2025/UserPhoto");
        if (!directory.exists()) {
            directory.mkdirs();
            log.debug("Не удалось сохранить файл");
            return null;
        }
        log.info("Сохраняем файл");
        File destFile = new File(directory.getAbsolutePath() + File.separator + fileName);
        file.transferTo(destFile);
        log.info("Создаем объект для БД photo");
        Photo photo = new Photo();
        photo.setFileName(fileName);
        photo.setFilePath(directory.getPath());
        photo.setFileSize(file.getSize());
        photo.setFileType(file.getContentType());
        return photo;
    }
}
