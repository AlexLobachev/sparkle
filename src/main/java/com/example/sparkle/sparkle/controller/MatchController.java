package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.model.Match;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.service.MatchService;
import com.example.sparkle.sparkle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sparkle/match")
public class MatchController {

    private final MatchService matchService;
    private final UserService userService;

    @Autowired
    public MatchController(MatchService matchService, UserService userService) {
        this.matchService = matchService;
        this.userService = userService;
    }

    /**
     * Получить следующего кандидата для свайпа
     */
    @GetMapping("/next-candidate/users")
    public ResponseEntity<?> getNextCandidate(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "40.0") double distance,
            @PageableDefault(page = 0, size = 1) Pageable pageable) {
        //обязательно добавить проверку существования пользователя и т д
        User user = userService.getUserById(userId).orElseThrow();
        return ResponseEntity.ok(matchService.getNextCandidate(user, distance, pageable));
    }

    /**
     * Выразить симпатию пользователю
     */
    @PostMapping("/like/{firstUser}/users/{secondUser}")
    public ResponseEntity<?> likeUser(@PathVariable Long firstUser, @PathVariable Long secondUser) {
        //обязательно добавить проверку существования пользователя и т д
        return ResponseEntity.ok(Match.toMatchDto(Match.toUserMatchDto(matchService.likeUser(firstUser, secondUser))));
    }

    /**
     * Посмотреть список текущих matches
     */
    @GetMapping("/current-matches/{userId}")
    public ResponseEntity<?> getCurrentMatches(@PathVariable Long userId) {
        return ResponseEntity.ok(matchService.getCurrentMatches(userId).stream().map(Match::toMatchDto));
    }

    /**
     * Посмотреть список кому поставил лайк
     */
    @GetMapping("/current-lake-your/{userId}")
    public ResponseEntity<?> getYourLikesUser(@PathVariable Long userId) {
        return ResponseEntity.ok(matchService.getYourLikesUser(userId).stream().map(Match::toMatchDto));
    }

    /**
     * Посмотреть кому я понравился
     */
    @GetMapping("/current-lake-who/{userId}")
    public ResponseEntity<?> getWhoLikedIt(@PathVariable Long userId) {
        return ResponseEntity.ok(matchService.getWhoLikedIt(userId).stream().map(user -> Match.toMatchDto(Match.toUserMatchDto(user.getFirstUser()))));
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
