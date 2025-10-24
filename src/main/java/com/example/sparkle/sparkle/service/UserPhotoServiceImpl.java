package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.UserPhoto;
import com.example.sparkle.sparkle.repository.UserPhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserPhotoServiceImpl implements UserPhotoService {

    private final UserPhotoRepository userPhotoRepository;

    @Autowired
    public UserPhotoServiceImpl(UserPhotoRepository userPhotoRepository) {
        this.userPhotoRepository = userPhotoRepository;
    }

    /**
     * Сохранение фото пользователя
     */
    @Override
    public UserPhoto saveUserPhoto(UserPhoto userPhoto) {
        return userPhotoRepository.save(userPhoto);
    }

    /**
     * Получение фото пользователя
     */
    @Override
    public UserPhoto getPhotoById(Long photoId) {
        return userPhotoRepository.findByPhotoId(photoId);
    }

    /**
     * Получение всех фото пользователя
     */
    @Override
    public List<UserPhoto> getAllPhotoByIdUser(Long userId) {
        return userPhotoRepository.findByUserId(userId);
    }

    /**
     * Удаление фото пользователя
     */
    @Transactional
    @Override
    public void deleteByPhotoId(Long photoId) {
        userPhotoRepository.deleteByPhotoId(photoId);
    }

}
