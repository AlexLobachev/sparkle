package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.match.MatchDto;
import com.example.sparkle.sparkle.dto.user.UserDto;
import com.example.sparkle.sparkle.dto.user.UserMatchDto;
import com.example.sparkle.sparkle.model.ChatDelete;
import com.example.sparkle.sparkle.model.Match;
import com.example.sparkle.sparkle.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    void deleteLike(Long secondUser);

    /**
     * Удалить метч
     */
    void deleteMatch(@PathVariable Long secondUser);

    /**
     * Если закончились пользователи в подборке, по запросу возвращает пропущенных
     */

    void reloadCandidate();
    /**
     * Поставить дизлайк пользователю
     */
    public void dislike(Long secondUser);


}
