package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.MatchDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MatchService {
    /**
     * Получить следующий кандидат для свайпа
     */

    ResponseEntity<MatchDTO> getNextCandidate();

    /**
     * Выразить симпатию пользователю
     */

    ResponseEntity<Boolean> likeUser(Long userId);

    /**
     * Посмотреть список текущих matches
     */

    ResponseEntity<List<MatchDTO>> getCurrentMatches();
}
