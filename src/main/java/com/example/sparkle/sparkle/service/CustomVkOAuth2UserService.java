package com.example.sparkle.sparkle.service;

import com.example.sparkle.sparkle.security.CustomOAuth2User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CustomVkOAuth2UserService extends DefaultOAuth2UserService {

    private static final OAuth2Error INVALID_USER_INFO_ERROR =
            new OAuth2Error("invalid_user_info", "Invalid response from VK", null);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            String response = oAuth2User.getAttribute("response");
            if (response == null || response.isEmpty()) {
                throw new OAuth2AuthenticationException(INVALID_USER_INFO_ERROR);
            }

            JsonNode node = objectMapper.readTree(response).get(0);
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", node.get("id").asText());
            attributes.put("first_name", node.has("first_name") ? node.get("first_name").asText() : "");
            attributes.put("last_name", node.has("last_name") ? node.get("last_name").asText() : "");

            String email = oAuth2User.getAttribute("email");
            if (email != null) {
                attributes.put("email", email);
            }

            Collection<? extends GrantedAuthority> authorities = new ArrayList<>(oAuth2User.getAuthorities());
            return new CustomOAuth2User(authorities, attributes, "id");
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(INVALID_USER_INFO_ERROR, e);
        }
    }
}