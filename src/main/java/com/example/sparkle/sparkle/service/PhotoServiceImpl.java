package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.Photo;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserPhoto;
import com.example.sparkle.sparkle.repository.PhotoRepository;
import com.example.sparkle.sparkle.repository.UserPhotoRepository;
import com.example.sparkle.sparkle.validator.ValidatorUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PhotoServiceImpl implements PhotoService {
    private static final String UPLOAD_DIR = "C:/Users/Mi/Documents/rep2025/UserPhoto/";
    private final PhotoRepository photoRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final UserService userService;
    private final UserPhotoService userPhotoService;
    private final ValidatorUser validatorUser;

    @Autowired
    public PhotoServiceImpl(PhotoRepository photoRepository,
                            UserService userService,
                            UserPhotoService userPhotoService,
                            ValidatorUser validatorUser,
                            UserPhotoRepository userPhotoRepository) {
        this.photoRepository = photoRepository;
        this.userService = userService;
        this.userPhotoService = userPhotoService;
        this.validatorUser = validatorUser;
        this.userPhotoRepository = userPhotoRepository;

    }


    /**
     * Загрузка фотографии пользователя
     */
    @Override
    public Photo uploadUserPhoto(MultipartFile multipartFile) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User currentUserEntity = userService.getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));

        if (multipartFile.getSize() > 5242880) { // 5MB
            throw new FileSizeLimitExceededException("Размер файла превышает допустимый", multipartFile.getSize(), 5242880);
        }
        Photo photo = uploadFile(multipartFile);
        assert photo != null;
        photoRepository.save(photo);
        log.info("Создаем объект для БД userPhoto");
        UserPhoto userPhoto = new UserPhoto();
        userPhoto.setUser(currentUserEntity);
        userPhoto.setPhoto(photo);
        log.info("Сохраняем объект для БД userPhoto");
        userPhotoService.saveUserPhoto(userPhoto);
        return photo;
    }

    /**
     * Удаление фотографии пользователя
     */
    @Override
    public void removeUserPhoto(Long photoId) throws IOException {
        Optional<UserPhoto> optionalPhoto = Optional.ofNullable(userPhotoRepository.findByPhotoId(photoId));
        if (optionalPhoto.isPresent()) {
            Photo userPhoto = optionalPhoto.get().getPhoto();
            // работаем с найденным фото
            Path photoPath = Paths.get(UPLOAD_DIR + userPhoto.getFileName());
            if (Files.exists(photoPath)) {
                Files.delete(photoPath);
            } else {
                throw new NotFound("Фото не найдено в хранилище");
            }
            userPhotoService.deleteByPhotoId(userPhoto.getId());
            photoRepository.deleteById(userPhoto.getId());
        } else {
            log.info("Фото не найдено d БД");
            throw new NotFound("Фото не найдено в хранилище");
        }


    }


    private Photo uploadFile(MultipartFile file) throws IOException {
        log.info("Проверяем, что файл не пустой");
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }
        log.info("Создаем уникальное имя файла");
        String originalFilename = file.getOriginalFilename();
        String fileName = String.join("-", UUID.randomUUID().toString(), originalFilename);
        String fileDirectory = "C:/Users/Mi/Documents/rep2025/UserPhoto/";
        log.info("Создаем путь для сохранения");
        File directory = new File(fileDirectory);
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
        photo.setUrl("/images/" + fileName);
        photo.setFileSize(file.getSize());
        photo.setFileType(file.getContentType());
        return photo;
    }
}
