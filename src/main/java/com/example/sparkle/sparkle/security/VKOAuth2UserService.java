package com.example.sparkle.sparkle.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class VKOAuth2UserService extends DefaultOAuth2UserService {

    private static final String ERROR_CODE = "vk_user_info_error";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        try {
            String response = oAuth2User.getAttribute("response").toString();
            if (!StringUtils.hasText(response) || response.equals("{}")) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(ERROR_CODE, "Empty or invalid response from VK", null)
                );
            }

            JsonNode node = objectMapper.readTree(response);
            if (node.isArray() && node.size() > 0) {
                JsonNode userNode = node.get(0);
                Map<String, Object> attributes = new HashMap<>();

                copyIfExists(userNode, attributes, "id", "id");
                copyIfExists(userNode, attributes, "first_name", "first_name");
                copyIfExists(userNode, attributes, "last_name", "last_name");

                // Email приходит отдельно, но положим в attributes
                String email = oAuth2User.getAttribute("email");
                if (email != null) {
                    attributes.put("email", email);
                }

                Collection<? extends GrantedAuthority> authorities = new ArrayList<>(oAuth2User.getAuthorities());

                return new CustomOAuth2User(authorities, attributes, "id");
            } else {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(ERROR_CODE, "Invalid VK response format: expected array with at least one user", null)
                );
            }
        } catch (IOException e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(ERROR_CODE, "Failed to parse VK response", null), e
            );
        } catch (Exception e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(ERROR_CODE, "Unexpected error during VK user info processing", null), e
            );
        }
    }

    private void copyIfExists(JsonNode node, Map<String, Object> map, String key, String targetKey) {

        if (node.has(key)) {
            map.put(targetKey, node.get(key).asText());
        }
    }
}