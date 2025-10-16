package com.example.sparkle.sparkle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class OAuth2Config {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
                ClientRegistration.withRegistrationId("your-provider")
                        .clientId("your-client-id")
                        .clientSecret("your-client-secret")
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .redirectUri("{baseUrl}/login/oauth2/code/your-provider")
                        .scope("openid", "profile", "email")
                        .authorizationUri("https://your-provider.com/authorize")
                        .tokenUri("https://your-provider.com/token")
                        .userInfoUri("https://your-provider.com/userinfo")
                        .jwkSetUri("https://your-provider.com/keys")
                        .build()
        );
    }
}
