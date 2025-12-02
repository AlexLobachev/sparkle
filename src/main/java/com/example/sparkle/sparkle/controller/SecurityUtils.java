package com.example.sparkle.sparkle.controller;

import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtils {
    public static String generateCspNonce() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
