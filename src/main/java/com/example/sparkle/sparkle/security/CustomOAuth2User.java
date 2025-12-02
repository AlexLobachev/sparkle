package com.example.sparkle.sparkle.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User extends DefaultOAuth2User {

    private final String id;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String nameAttributeKey) {
        super(authorities, attributes, nameAttributeKey);
        this.id = attributes.get("id").toString();
    }

    @Override
    public String getName() {
        return id;
    }

    public String getFirstName() {
        return (String) getAttributes().get("first_name");
    }

    public String getLastName() {
        return (String) getAttributes().get("last_name");
    }

    public String getEmail() {
        return (String) getAttributes().get("email");
    }
}