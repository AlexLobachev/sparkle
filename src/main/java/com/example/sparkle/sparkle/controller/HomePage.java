package com.example.sparkle.sparkle.controller;

import com.example.sparkle.sparkle.exception.NotFound;
import com.example.sparkle.sparkle.model.Chat;
import com.example.sparkle.sparkle.model.Status;
import com.example.sparkle.sparkle.model.User;
import com.example.sparkle.sparkle.service.ChatService;
import com.example.sparkle.sparkle.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HomePage {

    private final UserService userService;
    private final ChatService chatService;

    private static final String VK_SDK_URL = "https://unpkg.com/@vkid/sdk/dist-sdk/umd/index.js";
    private static final String NGROK_URL = "https://subjectional-manie-creaky.ngrok-free.dev";

    private String generateCspNonce() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/entrance";
    }


    @GetMapping("/entrance")
    public String showEntrance(Model model) {
        model.addAttribute("vkSdkUrl", VK_SDK_URL);
        model.addAttribute("ngrokUrl", NGROK_URL);
        model.addAttribute("cspNonce", generateCspNonce());
        model.addAttribute("cspNonce", UUID.randomUUID().toString());
        return "vk-entrance";
    }

    @GetMapping("/chat/{chatId}")
    public String getChatPage(@PathVariable Long chatId,
                              Model model,
                              Authentication authentication) {
        User currentUser = ((User) authentication.getPrincipal());

        Chat chat = chatService.getChatById(chatId);
        User interlocutor = chat.getSender().getId().equals(currentUser.getId())
                ? chat.getReceiver()
                : chat.getSender();

        model.addAttribute("chatId", chatId);
        model.addAttribute("interlocutorName", interlocutor.getUsername());
        return "chat"; // шаблон chat.html
    }



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

    @GetMapping("/settings/profile")
    public String profileSettingsDuringRegistration(@AuthenticationPrincipal UserDetails userDetails,
                                                    HttpServletRequest request,
                                                    Model model) {
        return settings(userDetails, request, model, "registration", null);
    }

    @GetMapping("/main/settings/profile")
    public String profileSettingsAfterRegistration(@AuthenticationPrincipal UserDetails userDetails,
                                                   HttpServletRequest request,
                                                   Model model) {
        return settings(userDetails, request, model, "settings-profile", null);
    }

    @GetMapping("/chats-matches")
    public String chatsMatches(@AuthenticationPrincipal UserDetails userDetails,
                               HttpServletRequest request,
                               Model model) {
        return settings(userDetails, request, model, "chats-matches", null);
    }

    @GetMapping("/profile-user/{id}")
    public String userProfile(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              HttpServletRequest request,
                              Model model) {
        return settings(userDetails, request, model, "profile-user", id);
    }

    @GetMapping("/main/profile")
    public String profileUser(@AuthenticationPrincipal UserDetails userDetails,
                              HttpServletRequest request,
                              Model model) {
        return settings(userDetails, request, model, "profile", null);
    }

    private String settings(UserDetails userDetails,
                            HttpServletRequest request,
                            Model model,
                            String page,
                            Long profileId) {
        Object csrfAttribute = request.getAttribute("_csrf");
        if (!(csrfAttribute instanceof org.springframework.security.web.csrf.CsrfToken csrfToken)) {
            log.warn("CSRF токен не найден в запросе: {}", request.getRequestURI());
            return "redirect:/";
        }

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

        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("csrfToken", csrfToken.getToken());
        model.addAttribute("user", authenticatedUser);
        model.addAttribute("userId", currentUserId);
        model.addAttribute("username", currentUsername);

        if (page.equals("profile-user") && profileId != null && !profileId.equals(currentUserId)) {
            model.addAttribute("profileId", profileId);
        }

        return page;
    }
}