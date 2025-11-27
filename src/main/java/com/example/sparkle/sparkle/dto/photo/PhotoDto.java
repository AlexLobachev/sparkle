package com.example.sparkle.sparkle.dto.photo;

import com.example.sparkle.sparkle.model.UserPhoto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PhotoDto {
    private Long id;
    private String url;
    private String fileName;

    public static PhotoDto toPhotoDto(UserPhoto userPhoto) {
        if (userPhoto == null) {
            return null;
        } else {
            return PhotoDto.builder()
                    .id(userPhoto.getPhoto().getId())
                    .url("/images/" + userPhoto.getPhoto().getFileName())
                    .fileName(userPhoto.getPhoto().getFileName())
                    .build();
        }




        //public static UserPhotoDto toUserPhotoDto(UserPhoto userPhoto) {
        //    UserPhotoDto userPhotoDto = new UserPhotoDto();
        //    userPhotoDto.setId(userPhoto.getPhoto().getId());
        //    userPhotoDto.setUrl("/images/" + userPhoto.getPhoto().getFileName());
        //    userPhotoDto.setFileName(userPhoto.getPhoto().getFileName());
        //    return userPhotoDto;
        //}

    }
}
