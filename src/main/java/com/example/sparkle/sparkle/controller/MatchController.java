package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.MatchDTO;
import com.example.sparkle.sparkle.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    private final MatchService matchService;

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Получить следующий кандидат для свайпа
     */
    @GetMapping("/next-candidate")
    public ResponseEntity<MatchDTO> getNextCandidate() {
        return ResponseEntity.ok(matchService.getNextCandidate()).getBody();
    }

    /**
     * Выразить симпатию пользователю
     */
    @PostMapping("/like/{userId}")
    public ResponseEntity<Boolean> likeUser(@PathVariable Long userId) {
        boolean isMatched = matchService.likeUser(userId).getBody();
        if (isMatched) {
            return ResponseEntity.status(201).body(true); // Если получился match
        }
        return ResponseEntity.ok(false); // Просто понравился
    }

    /**
     * Посмотреть список текущих matches
     */
    @GetMapping("/current-matches")
    public ResponseEntity<List<MatchDTO>> getCurrentMatches() {
        return ResponseEntity.ok(matchService.getCurrentMatches()).getBody();
    }
}
