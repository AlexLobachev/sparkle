package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.user.UserDto;
import com.example.sparkle.sparkle.exception.BadRequest;
import com.example.sparkle.sparkle.exception.Conflict;
import com.example.sparkle.sparkle.exception.NoContent;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.Match;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.repository.MatchRepository;
import com.example.sparkle.sparkle.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Класс для обработки лайков
 */
@Service
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public MatchServiceImpl(MatchRepository matchRepository, UserRepository userRepository, UserService userService) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Получить следующий кандидат для свайпа
     */

    public UserDto getNextCandidate(Long userId, double distance, Pageable pageable) {
        User user = userService.getUserById(userId).orElseThrow();
        distance = distance / 100;
        if (user.getCity() == null) {
            return getNextCandidateOffCity(user, pageable);
        }
        double x = user.getCity().getLocation().getCoordinate().x;
        double y = user.getCity().getLocation().getCoordinate().y;

        List<String> interests = new ArrayList<>();
        user.getInterests().forEach(e -> interests.add(e.getInterest().toString()));
        Page<User> candidates = userRepository.findUsersNearLocation(
                x, y, distance, user.getPreferredGender().toString(), interests, user.getId(), pageable);
        return candidates.get().map(User::toUserDtoBuilder).findFirst().orElseThrow(() -> new NoContent("Нет доступных кандидатов"));

    }

    /**
     * Получить следующий кандидат для свайпа (если пользователь не указывал город, гендер, интересы)
     */
    private UserDto getNextCandidateOffCity(User user, Pageable pageable) {
        List<String> interests = new ArrayList<>();
        user.getInterests().forEach(e -> interests.add(e.getInterest().toString()));
        Page<User> candidates = userRepository.findUsersOffLocation(user.getPreferredGender().toString(), interests, user.getId(), pageable);
        return candidates.get().map(User::toUserDtoBuilder).findFirst().orElseThrow(() -> new NoContent("Нет доступных кандидатов"));

    }


    /**
     * Выразить симпатию пользователю
     */
    @Transactional
    public User likeUser(Long firstUser, Long secondUser) {
        if (firstUser.equals(secondUser)) {
            throw new BadRequest("Первый пользователь равен второму");
        }

        User user1 = userService.getUserById(firstUser)
                .orElseThrow(() -> new NotFound("User " + firstUser + " not found"));
        User user2 = userService.getUserById(secondUser)
                .orElseThrow(() -> new NotFound("User " + secondUser + " not found"));


        Optional<Match> existingMatch = Optional.ofNullable(matchRepository
                .findByFirstUserIdAndSecondUserId(firstUser, secondUser));

        if (existingMatch.isPresent()) {
            Match match = existingMatch.get();
            if (match.getFirstUser().getId().equals(firstUser)) {
                throw new Conflict("У вас уже есть совпадение");
            }
        } else {
            Match newMatch = new Match();
            newMatch.setFirstUser(user1);
            newMatch.setSecondUser(user2);
            newMatch.setCreatedAt(LocalDateTime.now());
            matchRepository.save(newMatch);
        }

        existingMatch = Optional.ofNullable(matchRepository
                .findByFirstUserIdAndSecondUserId(firstUser, secondUser));
        Optional<Match> newExistingMatch = Optional.ofNullable(matchRepository
                .findByFirstUserIdAndSecondUserId(secondUser, firstUser));

        if (existingMatch.isPresent() && newExistingMatch.isPresent()) {
            matchRepository.update(firstUser, secondUser);
        }

        return user2;

    }


    /**
     * Посмотреть список текущих matches
     */

    public List<Match> getCurrentMatches(Long userId) {
        userService.getUserById(userId);
        List<Match> matches = matchRepository.findByFirstUserIdAndMatchStatus(userId, true);
        if (matches.isEmpty())
            throw new NoContent();
        return matches;
    }

    /**
     * Посмотреть список кому поставил лайк
     */

    public List<Match> getYourLikesUser(Long userId) {
        userService.getUserById(userId);
        List<Match> matches = matchRepository.findByFirstUserIdAndMatchStatus(userId, false);
        if (matches.isEmpty())
            throw new NoContent();
        return matches;
    }

    /**
     * Посмотреть кому я понравился
     */

    public List<Match> getWhoLikedIt(Long userId) {
        userService.getUserById(userId);
        List<Match> matches = matchRepository.findBySecondUserIdAndMatchStatus(userId, false);
        if (matches.isEmpty())
            throw new NoContent();
        return matches;
    }

    /**
     * Удалить лайк у пользователя
     */
    @Transactional
    public void deleteLike(Long firstUser, Long secondUser) {
        if (firstUser.equals(secondUser))
            throw new BadRequest("firstUser = secondUser - Ошибка!");
        userService.getUserById(firstUser);
        userService.getUserById(secondUser);
        matchRepository.deleteByFirstUserIdAndSecondUserId(firstUser, secondUser);
        matchRepository.deleteByFirstUserIdAndSecondUserId(secondUser, firstUser);
        if (!matchRepository.findLike(firstUser, secondUser).isEmpty())
            throw new Conflict("Ошибка удаления метча/лайка");

    }
}
