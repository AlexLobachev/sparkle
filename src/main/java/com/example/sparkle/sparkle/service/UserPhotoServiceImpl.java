package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.model.UserPhoto;
import com.example.sparkle.sparkle.repository.UserPhotoRepository;
import com.example.sparkle.sparkle.validator.ValidatorPhoto;
import com.example.sparkle.sparkle.validator.ValidatorUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class UserPhotoServiceImpl implements UserPhotoService {

    private final UserPhotoRepository userPhotoRepository;
    private final UserService userService;
    private final ValidatorPhoto validatorPhoto;
    private final ValidatorUser validatorUser;

    @Autowired
    public UserPhotoServiceImpl(UserPhotoRepository userPhotoRepository,
                                UserService userService,
                                ValidatorPhoto validatorPhoto,
                                ValidatorUser validatorUser) {
        this.userPhotoRepository = userPhotoRepository;
        this.userService = userService;
        this.validatorPhoto = validatorPhoto;
        this.validatorUser = validatorUser;
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
    public UserPhoto getPhotoById(Long userId, Long photoId) {
        userService.getUserById(userId).orElseThrow();
        UserPhoto userPhoto = userPhotoRepository.findByUserIdAndPhotoId(userId,photoId);
        validatorPhoto.photoNoContent(userPhoto);
        validatorUser.userForbidden(userPhoto.getUser(), userId);
        return userPhoto;
    }

    /**
     * Получение всех фото пользователя
     */
    @Override
    public List<UserPhoto> getAllPhotoByIdUser(Long userId) {
        User user = userService.getUserById(userId).orElseThrow();
        List<UserPhoto> userPhotoList = userPhotoRepository.findByUserId(userId);
        validatorPhoto.listPhotoNoContent(userPhotoList);
        validatorPhoto.photoForbidden(userPhotoList, userId);
        return userPhotoList;
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
