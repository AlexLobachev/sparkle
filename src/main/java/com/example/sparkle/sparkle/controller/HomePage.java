package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.Status;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Главный контроллер для отображения страниц приложения.
 * Объединяет маршруты для /main, /settings, /chats-matches и других.
 * Использует единый шаблонизатор через метод settings(...).
 */
@Controller
@Slf4j
@RequiredArgsConstructor // Убираем @Autowired с конструктором — более современный подход
public class HomePage {

    private final UserService userService;

    /**
     * Главная страница (до входа)
     */
    @GetMapping("/")
    public String home() {
        return "entrance";
    }

    /**
     * Основная страница приложения после входа.
     * Проверяет статус пользователя: если DRAFT — отправляет на донастройку профиля.
     */
    @GetMapping("/main")
    public String main(@AuthenticationPrincipal UserDetails userDetails,
                       HttpServletRequest request,
                       Model model) {
        User user = userService.getUserByUserName(userDetails.getUsername())
                .orElseThrow(() -> new NotFound("Пользователь не найден"));

        if (user.getStatus() == Status.DRAFT) {
            return settings(userDetails, request, model, "registration", null);
        }

        return settings(userDetails, request, model, "main-page", null);
    }

    /**
     * Настройка профиля во время регистрации
     */
    @GetMapping("/settings/profile")
    public String profileSettingsDuringRegistration(@AuthenticationPrincipal UserDetails userDetails,
                                                    HttpServletRequest request,
                                                    Model model) {
        return settings(userDetails, request, model, "registration", null);
    }

    /**
     * Настройка профиля после регистрации
     */
    @GetMapping("/main/settings/profile")
    public String profileSettingsAfterRegistration(@AuthenticationPrincipal UserDetails userDetails,
                                                   HttpServletRequest request,
                                                   Model model) {
        return settings(userDetails, request, model, "settings-profile", null);
    }

    /**
     * Страница чатов и мэтчей
     */
    @GetMapping("/chats-matches")
    public String chatsMatches(@AuthenticationPrincipal UserDetails userDetails,
                               HttpServletRequest request,
                               Model model) {
        return settings(userDetails, request, model, "chats-matches", null);
    }

    /**
     * Просмотр профиля другого пользователя
     */
    @GetMapping("/profile-user/{id}")
    public String userProfile(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              HttpServletRequest request,
                              Model model) {
        return settings(userDetails, request, model, "profile-user", id);
    }


    /**
     * Просмотр собственного профиля
     */
    @GetMapping("/main/profile")
    public String profileUser(@AuthenticationPrincipal UserDetails userDetails,
                              HttpServletRequest request,
                              Model model) {
        return settings(userDetails, request, model, "profile", null);
    }

    /**
     * Единый метод для настройки модели и возврата страницы.
     *
     * @param userDetails текущий пользователь
     * @param request     HTTP-запрос (для CSRF)
     * @param model       модель для передачи данных в шаблон
     * @param page        имя Thymeleaf-шаблона
     * @param profileId   ID пользователя, чей профиль просматривается (если есть)
     * @return имя шаблона для отображения
     */
    private String settings(UserDetails userDetails,
                            HttpServletRequest request,
                            Model model,
                            String page,
                            Long profileId) {
        // Извлечение CSRF-токена
        Object csrfAttribute = request.getAttribute("_csrf");
        if (!(csrfAttribute instanceof org.springframework.security.web.csrf.CsrfToken csrfToken)) {
            log.warn("CSRF токен не найден в запросе: {}", request.getRequestURI());
            return "redirect:/";
        }

        // Проверка и извлечение ID текущего пользователя
        if (!(userDetails instanceof User authenticatedUser)) {
            throw new NotFound("Пользователь не авторизован или не является экземпляром User");
        }
        Long currentUserId = authenticatedUser.getId();
        if (currentUserId == null) {
            throw new NotFound("ID пользователя не найден");
        }

        String currentUsername = userDetails.getUsername();
        if (currentUsername == null) {
            throw new NotFound("Имя пользователя не найдено");
        }
        // Передача данных в модель
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("csrfToken", csrfToken.getToken());
        model.addAttribute("user", authenticatedUser);
        model.addAttribute("userId", currentUserId);
        model.addAttribute("username", currentUsername);

        // Передача ID профиля, если это страница просмотра профиля и ID другого пользователя
        if (page.equals("profile-user") && profileId != null && !profileId.equals(currentUserId)) {
            model.addAttribute("profileId", profileId);
        }

        return page;
    }


}