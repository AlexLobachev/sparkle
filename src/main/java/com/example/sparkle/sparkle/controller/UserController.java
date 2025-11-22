package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.dto.LocationRequestDto;
import com.example.sparkle.sparkle.dto.user.UserDtoRegister;
import com.example.sparkle.sparkle.dto.user.UserDtoUpdate;
import com.example.sparkle.sparkle.dto.user.UserMapper;
import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.service.GeocodingService;
import com.example.sparkle.sparkle.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Класс-контроллер для работы с пользователями
 */
@RestController
@RequestMapping("/sparkle/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final GeocodingService geocodingService;


    @Autowired
    public UserController(UserService userService, GeocodingService geocodingService) {
        this.userService = userService;
        this.geocodingService = geocodingService;

    }

    /**
     * Регистрация нового пользователя
     * Пользователь вводит Имя, Пол, Дату рождения
     */
    @PostMapping("/register/next-page")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDtoRegister userDtoRegister) {
        User user = userService.registerUser(UserMapper.toUser(userDtoRegister)).orElseThrow();
        return ResponseEntity.ok(UserMapper.toUserDtoRegister(user));

    }

    /**
     * Редактирование профиля пользователя
     */
    @PatchMapping("/update-profile/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable @Min(1) Long userId,
            @Valid @RequestBody UserDtoUpdate userDtoUpdate) {

        return ResponseEntity.ok(userService.updateUserProfile(userId, userDtoUpdate)
                .orElseThrow());
    }

    @PatchMapping("/setup-profile")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> setupUserProfile(@Valid @RequestBody UserDtoUpdate userDtoUpdate) {

        return ResponseEntity.ok(UserMapper.toUserDto(userService.setupUserProfile(userDtoUpdate).orElseThrow()));
    }

    /**
     * Получение всех пользователей
     */
    @GetMapping
    public ResponseEntity<?> getUserAll() {
        return ResponseEntity.ok(userService.getUserAll());
    }

    /**
     * Получение пользователя по ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable @Min(1) Long userId) {

        return ResponseEntity.ok(userService.getUserById(userId));

    }

    /**
     * Удаление пользователя по ID
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable @Min(1) Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update-email")
    public String updateEmail(@RequestParam String email, Authentication auth) {
        OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
        User user = userService.getUserByExternalId(oauth2User.getAttribute("externalId").toString()).orElseThrow(() -> new NotFound("Пользователь не найден"));
        user.setEmail(email);
        user.setEmailPending(false);
        userService.updateUserProfile(user.getId(), UserMapper.toUserDtoUpdate(user));
        return ResponseEntity.ok("redirect:/main").getBody();
    }


    //@GetMapping("/main")
    //public String showMainPage(Model model, Authentication authentication) {
    //    // Получаем текущего пользователя из SecurityContext
    //    User user = (User) authentication.getPrincipal();
//
    //    if (user.getStatus() == Status.DRAFT) {
    //        return "redirect:/profile/setup";  // обратно к заполнению профиля
    //    }
//
    //    // Добавляем в модель
    //    model.addAttribute("user", user);
//
    //    return "index2"; // имя вашего шаблона
    //}


    /**
     * Сохранение локации
     */
    //@PostMapping("/location")
    @PostMapping("/location/{userId}")
    public ResponseEntity<?> saveUserLocation(@RequestBody LocationRequestDto location, @PathVariable Long userId
            /*@AuthenticationPrincipal UserDetails userDetails*/) {

        return ResponseEntity.ok(UserMapper.toUserDto(userService.saveUserLocation(location, userId).orElseThrow()));
    }


    @GetMapping("/csrf-token")
    public Map<String, String> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrf = (CsrfToken) request.getAttribute("_csrf");
        return Map.of("token", csrf.getToken());
    }


}