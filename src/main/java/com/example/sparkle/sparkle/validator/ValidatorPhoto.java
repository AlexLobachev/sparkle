package com.example.sparkle.sparkle.validator;

import com.example.sparkle.sparkle.exception.Forbidden;
import com.example.sparkle.sparkle.exception.NoContent;
import com.example.sparkle.sparkle.model.UserPhoto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ValidatorPhoto {


    public void listPhotoNoContent(List<UserPhoto> userPhotoList) {
        if (userPhotoList.isEmpty()) {
            log.info("Пришел пустой массив");
            throw new NoContent();
        }
    }
    public void photoNoContent(UserPhoto userPhoto) {
        if (userPhoto==null) {
            log.info("Фото отсутствует");
            throw new NoContent();
        }
    }

    public void photoForbidden(List<UserPhoto> userPhotoList, Long userId) {
        if (userPhotoList.stream().noneMatch(userPhoto -> userPhoto.getUser().getId().equals(userId))) {
            log.info("Фото не принадлежит пользователю с ID = {}", userId);
            throw new Forbidden("Доступ запрещен");

        }
    }
}
