package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.model.Match;
import com.example.sparkle.sparkle.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

/**
 * Класс-контроллер для обработки лайков
 */
@RestController
@RequestMapping("/sparkle/users/match")
public class MatchController {

    private final MatchService matchService;

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;

    }

    /**
     * Получить следующего кандидата для свайпа
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/next-candidate")
    public ResponseEntity<?> getNextCandidate(
            @RequestParam(defaultValue = "40.0") double distance,
            @PageableDefault(page = 0, size = 1) Pageable pageable) {
        return ResponseEntity.ok(matchService.getNextCandidate(distance, pageable));
    }

    /**
     * Выразить симпатию пользователю
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/like/{secondUser}")
    public ResponseEntity<?> likeUser(@PathVariable Long secondUser) {
        return ResponseEntity.ok(matchService.likeUser(secondUser));
    }

    /**
     * Посмотреть список текущих matches
     */
    @GetMapping("/current-matches")
    public ResponseEntity<?> getCurrentMatches() {
        return ResponseEntity.ok(matchService.getCurrentMatches());
    }

    /**
     * Посмотреть список кому поставил лайк
     */
    @GetMapping("/like/current-lake-your")
    public ResponseEntity<?> getYourLikesUser() {
        return ResponseEntity.ok(matchService.getYourLikesUser());
    }

    /**
     * Посмотреть кому я понравился
     */
    @GetMapping("/like/current-lake-who")
    public ResponseEntity<?> getWhoLikedIt() {
        //return ResponseEntity.ok(matchService.getWhoLikedIt().stream().map(user -> Match.toMatchDto(Match.toUserMatchDto(user.getFirstUser()))));
        return ResponseEntity.ok(matchService.getWhoLikedIt());

    }

    /**
     * Удалить лайк у пользователя
     */
    @DeleteMapping("/like/{firstUser}/users/{secondUser}")
    public ResponseEntity<?> deleteLike(@PathVariable Long firstUser, @PathVariable Long secondUser) {
        matchService.deleteLike(firstUser, secondUser);
        return ResponseEntity.ok().build();
    }


}
