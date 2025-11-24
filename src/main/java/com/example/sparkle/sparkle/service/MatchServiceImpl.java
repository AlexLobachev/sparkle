package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.match.MatchDto;
import com.example.sparkle.sparkle.dto.match.MatchMapper;
import com.example.sparkle.sparkle.dto.user.UserMatchDto;
import com.example.sparkle.sparkle.exception.BadRequest;
import com.example.sparkle.sparkle.exception.Conflict;
import com.example.sparkle.sparkle.exception.NoContent;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.CandidateBatch;
import com.example.sparkle.sparkle.model.Match;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.repository.CandidateBatchRepository;
import com.example.sparkle.sparkle.repository.MatchRepository;
import com.example.sparkle.sparkle.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Класс для обработки лайков
 */
@Service
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CandidateBatchRepository candidateBatchRepository;

    @Autowired
    public MatchServiceImpl(MatchRepository matchRepository,
                            UserRepository userRepository,
                            UserService userService,
                            CandidateBatchRepository candidateBatchRepository) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.candidateBatchRepository = candidateBatchRepository;
    }

    /**
     * Получить следующий кандидат для свайпа
     */

    public UserMatchDto getNextCandidate(double distance, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));


        distance = distance / 100;

        //if (user.getCity() == null) {
        //    return getNextCandidateOffCity(user, pageable);
        //}

        double x = user.getCity().getLocation().getCoordinate().x;
        double y = user.getCity().getLocation().getCoordinate().y;
        final double finalDistance = distance;
        List<String> interests = user.getInterests().stream()
                .map(e -> e.getInterest().toString())
                .toList();

        // Получаем текущую пачку кандидатов для пользователя
        CandidateBatch batch = candidateBatchRepository.findByUserIdAndExpiresAtAfter(
                user.getId(), LocalDateTime.now()
        ).orElseGet(() -> {

            List<Long> newBatch = fetchNewCandidateBatch(x, y, finalDistance, user, interests);
            log.debug(">>>>>>>>>>>"+newBatch.toString());
            if (newBatch.isEmpty()) {
                throw new NoContent("Нет кандидатов для формирования пачки");
            }

            CandidateBatch newBatchEntity = new CandidateBatch();
            newBatchEntity.setUser(user);
            newBatchEntity.setCandidateIds(newBatch);
            newBatchEntity.setExpiresAt(LocalDateTime.now().plusHours(1));
            saveBatch(newBatchEntity);
            return newBatchEntity;
        });

        // Берём следующего кандидата из пачки
        if (batch.getCurrentIndex() >= batch.getCandidateIds().size()) {
            List<Long> newBatch = fetchNewCandidateBatch(x, y, distance, user, interests);

            if (newBatch.isEmpty()) {
                throw new NoContent("Нет доступных кандидатов");
            }

            batch.getCandidateIds().clear();
            batch.setCandidateIds(newBatch);
            batch.setCurrentIndex(0);
            saveBatch(batch);
        }

        Long candidateId = batch.getCandidateIds().get(batch.getCurrentIndex());
        batch.setCurrentIndex(batch.getCurrentIndex() + 1);
        saveBatch(batch);

        // Возвращаем DTO кандидата
        return userRepository.findById(candidateId)
                .map(UserMatchDto::toUserMatchDto)
                .orElseThrow(() -> new NoContent("Кандидат не найден"));
    }


    /**
     * Получить следующий кандидат для свайпа (если пользователь не указывал город, гендер, интересы)
     */
    private UserMatchDto getNextCandidateOffCity(User user, Pageable pageable) {
        List<String> interests = new ArrayList<>();
        user.getInterests().forEach(e -> interests.add(e.getInterest().toString()));
        Page<User> candidates = userRepository.findUsersOffLocation(user.getPreferredGender().toString(), interests, user.getId(), pageable);
        //return candidates.get().map(MatchMapper::toMathDto).findFirst().orElseThrow(() -> new NoContent("Нет доступных кандидатов"));
        return candidates.get().map(UserMatchDto::toUserMatchDto).findFirst().orElseThrow(() -> new NoContent("Нет доступных кандидатов"));

    }


    /**
     * Выразить симпатию пользователю
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public UserMatchDto likeUser(Long secondUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user1 = userService.getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));

        if (user1.getId().equals(secondUser)) {
            throw new BadRequest("Первый пользователь равен второму");
        }

        User user2 = userRepository.findById(secondUser)
                .orElseThrow(() -> new NotFound("User " + secondUser + " not found"));


        Optional<Match> existingMatch = Optional.ofNullable(matchRepository
                .findByFirstUserIdAndSecondUserId(user1.getId(), secondUser));

        if (existingMatch.isPresent()) {
            Match match = existingMatch.get();
            if (match.getFirstUser().getId().equals(user1.getId())) {
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
                .findByFirstUserIdAndSecondUserId(user1.getId(), secondUser));
        Optional<Match> newExistingMatch = Optional.ofNullable(matchRepository
                .findByFirstUserIdAndSecondUserId(secondUser, user1.getId()));

        if (existingMatch.isPresent() && newExistingMatch.isPresent()) {
            matchRepository.update(user1.getId(), secondUser);
        }

        return UserMatchDto.toUserMatchDto(user2);

    }


    /**
     * Посмотреть список текущих matches
     */

    public List<MatchDto> getCurrentMatches() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));

        List<Match> matches = matchRepository.findByFirstUserIdAndMatchStatus(user.getId(), true);

        if (matches.isEmpty())
            throw new NoContent();
        return matches.stream().map(MatchMapper::toMathDto).toList();
    }

    /**
     * Посмотреть список кому поставил лайк
     */

    public List<MatchDto> getYourLikesUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
        List<Long> matches = matchRepository.findSecondUsersByFirstUserIdAndMatchStatus(user.getId(), false);
        if (matches.isEmpty())
            throw new NoContent();
        return userRepository.findAllById(matches).stream().map(MatchMapper::toMathDto).toList();
    }

    /**
     * Посмотреть кому я понравился
     */

    public List<MatchDto> getWhoLikedIt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));

        List<Match> matches = matchRepository.findByFirstUserIdAndMatchStatus(user.getId(), false);
        if (matches.isEmpty())
            throw new NoContent();
        return matches.stream().map(MatchMapper::toMathDto).toList();
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


    private List<Long> fetchNewCandidateBatch(
            double x, double y, double distance, User user, List<String> interests) {
        log.debug(">>>>>>>>>>>"+x);
        log.debug(">>>>>>>>>>>"+y);
        log.debug(">>>>>>>>>>>"+distance);
        log.debug(">>>>>>>>>>>"+user.getId());
        log.debug(">>>>>>>>>>>"+interests);
        Set<Long> excludedIds = new HashSet<>(
                candidateBatchRepository.findAllCandidateIdsByUserId(user.getId())
        );
        excludedIds.add(user.getId());
        log.debug(">>>>>>>>>>>excludedIds"+excludedIds.toString());

        // Дополнительно исключаем кандидатов из текущей активной пачки
        Optional<CandidateBatch> currentBatch = candidateBatchRepository
                .findByUserIdAndExpiresAtAfter(user.getId(), LocalDateTime.now());
        currentBatch.ifPresent(candidateBatch -> excludedIds.addAll(candidateBatch.getCandidateIds()));

        return userRepository.findCandidateIdsNearLocation(
                x, y, distance,
                user.getPreferredGender().toString(),
                interests,
                user.getId(),
                excludedIds.stream().toList()
        );
    }

    private void saveBatch(CandidateBatch batch) {
        // Удаляем дубликаты из списка
        Set<Long> uniqueIds = new LinkedHashSet<>(batch.getCandidateIds());
        batch.setCandidateIds(new ArrayList<>(uniqueIds));

        candidateBatchRepository.save(batch);
    }


}
