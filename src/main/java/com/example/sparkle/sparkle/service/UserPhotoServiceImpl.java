package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserPhoto;
import com.example.sparkle.sparkle.repository.UserPhotoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class UserPhotoServiceImpl implements UserPhotoService {
    private static final String UPLOAD_DIR = "uploads/";
    private final UserPhotoRepository userPhotoRepository;

    @Autowired
    public UserPhotoServiceImpl(UserPhotoRepository userPhotoRepository) {
        this.userPhotoRepository = userPhotoRepository;
    }

    /**
     * Загрузка фотографии пользователя
     */
    @Override
    public MultipartFile uploadUserPhoto(MultipartFile multipartFile, Long userId) throws IOException {
        UserPhoto userPhoto = uploadFile(multipartFile);
        assert userPhoto != null;   //надо добавить проверку на существование пользователя
        userPhoto.getUser().setId(userId);
        userPhotoRepository.save(userPhoto);
        return null;
    }

    /**
     * Удаление фотографии пользователя
     */
    @Override
    public void removeUserPhoto(Long photoId) {

    }


    private UserPhoto uploadFile(MultipartFile file) throws IOException {
        // Проверяем, что файл не пустой
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }

        // Создаем уникальное имя файла
        String originalFilename = file.getOriginalFilename();
        String fileName = String.join("-", UUID.randomUUID().toString(), originalFilename);

        // Создаем путь для сохранения
        File directory = new File("C:/Users/Mi/Documents/rep2025/UserPhoto");
        if (!directory.exists()) {
            directory.mkdirs();
            log.debug("Не удалось сохранить файл");
            return null;

        }


        // Сохраняем файл
        File destFile = new File(directory.getAbsolutePath() + File.separator + fileName);
        file.transferTo(destFile);

        // Создаем объект для БД
        UserPhoto userPhoto = new UserPhoto();
        userPhoto.setFileName(originalFilename);
        userPhoto.setFilePath(directory.getPath());
        userPhoto.setFileSize(file.getSize());
        userPhoto.setFileType(file.getContentType());
        return userPhoto;
    }
}
