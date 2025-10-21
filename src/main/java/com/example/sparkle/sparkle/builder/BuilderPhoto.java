package com.example.sparkle.sparkle.builder;

import com.example.sparkle.sparkle.model.Photo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@Slf4j
public class BuilderPhoto {
    public static ResponseEntity<?> photoBuilder(Photo photo) throws IOException {
        try {
            Path resource = Paths.get(photo.getFilePath(), photo.getFileName());
            byte[] imageData = Files.readAllBytes(resource);
            log.info("Установка заголовка типа контента");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return ResponseEntity.ok().headers(headers).body(imageData);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Ошибка вывода фото", HttpStatus.NOT_FOUND);
        }

    }
}
