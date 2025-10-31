package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.user.UserDto;
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

    UserDto getNextCandidate(Long user, double distance, Pageable pageable);

    /**
     * Выразить симпатию пользователю
     */

    User likeUser(Long firstUser, Long secondUser);

    /**
     * Посмотреть список текущих matches
     */

    List<Match> getCurrentMatches(Long userId);

    List<Match> getYourLikesUser(Long userId);

    /**
     * Посмотреть кому я понравился
     */

    List<Match> getWhoLikedIt(Long userId);

    /**
     * Удалить лайк у пользователя
     */

    void deleteLike(Long firstUser, Long secondUser);
}
