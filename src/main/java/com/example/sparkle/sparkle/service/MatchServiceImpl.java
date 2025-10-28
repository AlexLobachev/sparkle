package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.user.UserDto;
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
import java.util.NoSuchElementException;

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

    public UserDto getNextCandidate(User user, double distance, Pageable pageable) {
        distance = distance / 100;
        double x = user.getCity().getLocation().getCoordinate().x;
        double y = user.getCity().getLocation().getCoordinate().y;
        List<String> interests = new ArrayList<>();
        user.getInterests().forEach(e -> interests.add(e.getInterest().toString()));
        Page<User> candidates = userRepository.findUsersNearLocation(
                x, y, distance, user.getGender().toString(), interests, user.getId(), pageable);
        return candidates.get().map(User::toUserDtoBuilder).findFirst().orElseThrow(() -> new NoSuchElementException("Нет доступных кандидатов"));

    }





    /**
     * Выразить симпатию пользователю
     */

    public User likeUser(Long firstUser, Long secondUser) {
        Match match = new Match();
        if (matchRepository.findLike(firstUser, secondUser) == null) {
            match.setCreatedAt(LocalDateTime.now());
            match.setFirstUser(userService.getUserById(firstUser).orElseThrow());
            match.setSecondUser(userService.getUserById(secondUser).orElseThrow());

            return matchRepository.save(match).getSecondUser();

        }
        //обновляем статус пользователю поставившему лайк
        match = matchRepository.findByFirstUserIdAndSecondUserId(secondUser, firstUser);
        match.setMatchStatus(true);
        matchRepository.save(match);
        //обновляем статус при совпадении
        match = new Match();
        match.setCreatedAt(LocalDateTime.now());
        match.setFirstUser(userService.getUserById(firstUser).orElseThrow());
        match.setSecondUser(userService.getUserById(secondUser).orElseThrow());
        match.setMatchStatus(true);
        return matchRepository.save(match).getSecondUser();


    }


    /**
     * Посмотреть список текущих matches
     */

    public List<Match> getCurrentMatches(Long userId) {
        return matchRepository.findByFirstUserIdAndMatchStatus(userId, true);
    }

    /**
     * Посмотреть список кому поставил лайк
     */

    public List<Match> getYourLikesUser(Long userId) {

        return matchRepository.findByFirstUserIdAndMatchStatus(userId, false);
    }

    /**
     * Посмотреть кому я понравился
     */

    public List<Match> getWhoLikedIt(Long userId) {
        return matchRepository.findBySecondUserIdAndMatchStatus(userId, false);
    }

    /**
     * Удалить лайк у пользователя
     */
    @Transactional
    public void deleteLike(Long firstUser, Long secondUser) {
        matchRepository.deleteByFirstUserIdAndSecondUserId(firstUser,secondUser);
        matchRepository.deleteByFirstUserIdAndSecondUserId(secondUser,firstUser);
    }
}
