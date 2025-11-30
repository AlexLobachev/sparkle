package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.dto.interest.UserInterestsDto;
import com.example.sparkle.sparkle.dto.match.MatchDto;
import com.example.sparkle.sparkle.dto.match.MatchMapper;
import com.example.sparkle.sparkle.dto.user.UserMatchDto;
import com.example.sparkle.sparkle.exception.BadRequest;
import com.example.sparkle.sparkle.exception.NoContent;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.*;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс для обработки лайков
 */
@Service
@Slf4j
public class MatchServiceImpl implements MatchService {
    private static final double DEFAULT_X = 37.6176;
    private static final double DEFAULT_Y = 55.7558;

    private final MatchRepository matchRepository;


    private final UserRepository userRepository;
    private final UserService userService;

    private final ChatServiceImpl chatServiceimpl;
    // Кэш кандидатов
    private final Map<Long, Deque<UserMatchDto>> candidateCache = new ConcurrentHashMap<>();
    // Кэш уже показанных кандидатов для каждого пользователя (на время сессии)
    private final Map<Long, Set<Long>> shownCandidates = new ConcurrentHashMap<>();


    @Autowired
    public MatchServiceImpl(MatchRepository matchRepository,
                            UserRepository userRepository,
                            UserService userService,
                            ChatServiceImpl chatServiceimpl) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.chatServiceimpl = chatServiceimpl;
    }

    /**
     * Получить следующий кандидат для свайпа
     */


    public UserMatchDto getNextCandidate(double distance, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
        Long userId = user.getId();

        // Получаем или создаём список уже показанных кандидатов для этого пользователя
        Set<Long> alreadyShown = shownCandidates.computeIfAbsent(userId, k -> new HashSet<>());

        Deque<UserMatchDto> candidates;

        // Загружаем кандидатов, если кэш пуст
        if (!candidateCache.containsKey(userId) || candidateCache.get(userId).isEmpty()) {
            loadNewCandidates(user, distance);
            candidates = candidateCache.get(userId);
        } else {
            candidates = candidateCache.get(userId);
        }

        // Ищем первого кандидата, которого ещё не показывали
        while (!candidates.isEmpty()) {
            UserMatchDto candidate = candidates.pollFirst();
            if (!alreadyShown.contains(candidate.getUserId())) {
                // Запоминаем, что показали этого кандидата
                alreadyShown.add(candidate.getUserId());
                return candidate;
            }
            // Если кандидат уже был показан — продолжаем поиск
        }

        // Если все кандидаты из кэша уже были показаны — перезагружаем
        shownCandidates.put(userId, new HashSet<>()); // Очищаем список показанных
        loadNewCandidates(user, distance);
        candidates = candidateCache.get(userId);

        // Повторно ищем первого нового кандидата
        while (!candidates.isEmpty()) {
            UserMatchDto candidate = candidates.pollFirst();
            if (!alreadyShown.contains(candidate.getUserId())) {
                alreadyShown.add(candidate.getUserId());
                return candidate;
            }
        }

        throw new NoContent("Кандидаты закончились");
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

        try {
            if (user1.getId().equals(secondUser)) {
                throw new BadRequest("Первый пользователь равен второму");
            }

            User user2 = userRepository.findById(secondUser)
                    .orElseThrow(() -> new NotFound("User " + secondUser + " not found"));

            // 1. Ищем лайк от user1 → secondUser
            Optional<Match> existingMatch = Optional.ofNullable(matchRepository
                    .findByFirstUserIdAndSecondUserId(user1.getId(), secondUser));

            // 2. Ищем лайк от secondUser → user1 (взаимный лайк)
            Optional<Match> reverseMatch = Optional.ofNullable(matchRepository
                    .findByFirstUserIdAndSecondUserId(secondUser, user1.getId()));

            if (existingMatch.isPresent()) {
                // Если лайк уже есть, проверяем статус
                Match match = existingMatch.get();
                if (match.getMatchStatus() == Match.MatchStatus.MATCHED) {
                    throw new NoContent("У вас уже есть взаимное совпадение (MATCHED)");
                } else if (match.getMatchStatus() == Match.MatchStatus.LIKE) {
                    // Если уже есть LIKE, но нет взаимности — обновляем только текущий
                    // (возможно, это повторный вызов, но логика требует обновления)
                }
            }

            // 3. Создаём новый лайк user1 → secondUser (если его не было)
            if (!existingMatch.isPresent()) {
                Match newMatch = new Match();
                newMatch.setFirstUser(user1);
                newMatch.setSecondUser(user2);
                newMatch.setCreatedAt(LocalDateTime.now());
                newMatch.setMatchStatus(Match.MatchStatus.LIKE);
                matchRepository.save(newMatch);
            }

            // 4. Проверяем взаимный лайк (secondUser → user1)
            if (reverseMatch.isPresent()) {
                Match reverse = reverseMatch.get();
                if (reverse.getMatchStatus() == Match.MatchStatus.LIKE) {
                    // 5. Если взаимный лайк есть — обновляем оба до MATCHED
                    matchRepository.updateStatus(user1.getId(), secondUser, Match.MatchStatus.MATCHED);
                    matchRepository.updateStatus(secondUser, user1.getId(), Match.MatchStatus.MATCHED);
                }
            }

            candidateCache.remove(user1.getId());
            return UserMatchDto.toUserMatchDto(user2);

        } catch (Exception e) {
            candidateCache.remove(user1.getId());
            throw e;
        }
    }


    /**
     * Посмотреть список текущих matches
     */

    public List<MatchDto> getCurrentMatches() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));

        List<Match> matches = matchRepository.findByFirstUserIdAndMatchStatus(user.getId(), Match.MatchStatus.MATCHED);

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
        List<Long> matches = matchRepository.findSecondUsersByFirstUserIdAndMatchStatus(user.getId(), Match.MatchStatus.LIKE.name());
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

        List<Match> matches = matchRepository.findByFirstUserIdAndMatchStatus(user.getId(), Match.MatchStatus.LIKE);
        if (matches.isEmpty())
            throw new NoContent();
        return matches.stream().map(MatchMapper::toMathDto).toList();
    }


    /**
     * Удалить лайк у пользователя
     */
    @Override
    @Transactional
    public void deleteLike(Long secondUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
        matchRepository.deleteByFirstUserIdAndSecondUserId(user.getId(), secondUser);
    }

    /**
     * Удалить метч
     */
    @Override
    @Transactional
    public void deleteMatch(Long secondUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUserName(currentUser.getUsername()).orElseThrow(() -> new NotFound("Пользователь не найден"));
        matchRepository.deleteByFirstUserIdAndSecondUserId(user.getId(), secondUser);
        //ДОДЕЛАТЬ ЧАТЫ УДАЛЕНИЕ
        Chat chat = chatServiceimpl.getChatByReceiverIdAndSenderId(user.getId(), secondUser);
        if (chat !=null) {
        chatServiceimpl.deleteChat(chat.getId());
        }

    }

    /**
     * Поставить дизлайк пользователю
     */
    @Override
    @Transactional
    public UserMatchDto dislike(Long secondId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();

        // Получаем текущего пользователя (кто ставит DISLIKE)
        User user1 = userService.getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
        try {
            if (user1.getId().equals(secondId)) {
                throw new BadRequest("Нельзя поставить DISLIKE самому себе");
            }

            // Получаем пользователя, которому ставим DISLIKE
            User user2 = userRepository.findById(secondId)
                    .orElseThrow(() -> new NotFound("Пользователь с ID " + secondId + " не найден"));

            // Ищем существующую запись: first_user = user1, second_user = user2
            Optional<Match> existingMatch = Optional.ofNullable(
                    matchRepository.findByFirstUserIdAndSecondUserId(user1.getId(), secondId)
            );

            if (existingMatch.isPresent()) {
                // Обновляем статус существующей записи на DISLIKE
                Match match = existingMatch.get();
                match.setMatchStatus(Match.MatchStatus.DISLIKE);
                match.setCreatedAt(LocalDateTime.now()); // обновляем время
                matchRepository.save(match);
            } else {
                // Создаём новую запись с статусом DISLIKE
                Match newMatch = new Match();
                newMatch.setFirstUser(user1);
                newMatch.setSecondUser(user2);
                newMatch.setCreatedAt(LocalDateTime.now());
                newMatch.setMatchStatus(Match.MatchStatus.DISLIKE);
                matchRepository.save(newMatch);
            }
            candidateCache.remove(user1.getId());
            // Возвращаем DTO пользователя, которому поставили DISLIKE
            return UserMatchDto.toUserMatchDto(user2);
        } catch (Exception e) {
            candidateCache.remove(user1.getId());
            throw e;
        }
    }


    private void loadNewCandidates(User user, double distance) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));
        double x = DEFAULT_X;
        double y = DEFAULT_Y;
        double finalDistance = 10000 / 100.0;
        if (currentUser.getCity()!=null) {
            x = currentUser.getCity().getLocation().getCoordinate().x;
            y = currentUser.getCity().getLocation().getCoordinate().y;
            finalDistance = distance / 100.0;
        }
        if (currentUser.getInterests().isEmpty()||currentUser.getInterests()==null) {
            currentUser.setInterests(UserInterest.toListUserInterest());
        }
        List<Long> candidateIds = userRepository.findCandidateIdsNearLocation(
                x, y, finalDistance,
                currentUser.getPreferredGender().toString(),
                currentUser.getInterests().stream()
                        .map(e -> e.getInterest().toString())
                        .toList(),
                user.getId()
        );

        List<UserMatchDto> candidates = userRepository.findAllById(candidateIds).stream().map(UserMatchDto::toUserMatchDto).toList();
        Deque<UserMatchDto> deque = new ArrayDeque<>(candidates);
        candidateCache.put(user.getId(), deque);
    }


    @Override
    @Transactional
    public void reloadCandidate() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUserName(currentUser.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));

        // Преобразуем enum в строку
        String status = Match.MatchStatus.DISLIKE.name();  // Возвращает "DISLIKE"

        matchRepository.deleteByFirstUserIdAndMatchStatus(
                user.getId(),
                status
        );

        candidateCache.remove(user.getId());
    }


}