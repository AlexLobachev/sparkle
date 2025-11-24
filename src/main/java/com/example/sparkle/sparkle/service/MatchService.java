package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.match.MatchDto;
import com.example.sparkle.sparkle.dto.user.UserDto;
import com.example.sparkle.sparkle.dto.user.UserMatchDto;
import com.example.sparkle.sparkle.model.Match;
import com.example.sparkle.sparkle.model.User;
import org.springframework.data.domain.Pageable;

import java.util.List;
/**
 * Интерфейс для обработки лайков
 */
public interface MatchService {
    /**
     * Получить следующий кандидат для свайпа
     */

    UserMatchDto getNextCandidate(double distance, Pageable pageable);

    /**
     * Выразить симпатию пользователю
     */

    UserMatchDto likeUser(Long secondUser);

    /**
     * Посмотреть список текущих matches
     */

    List<MatchDto> getCurrentMatches();
    /**
     * Посмотреть кому отправили лайк
     */

    List<MatchDto> getYourLikesUser();

    /**
     * Посмотреть кому я понравился
     */

    List<MatchDto> getWhoLikedIt();

    /**
     * Удалить лайк у пользователя
     */

    void deleteLike(Long firstUser, Long secondUser);
}
