package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.MatchDTO;
import com.example.sparkle.sparkle.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;

    @Autowired
    public MatchServiceImpl (MatchRepository matchRepository){
        this.matchRepository = matchRepository;
    }
    /**
     * Получить следующий кандидат для свайпа
     */

    public ResponseEntity<MatchDTO> getNextCandidate() {
        return null;
    }

    /**
     * Выразить симпатию пользователю
     */

    public ResponseEntity<Boolean> likeUser(Long userId) {
        return null;
    }

    /**
     * Посмотреть список текущих matches
     */

    public ResponseEntity<List<MatchDTO>> getCurrentMatches() {
        return null;
    }
}
