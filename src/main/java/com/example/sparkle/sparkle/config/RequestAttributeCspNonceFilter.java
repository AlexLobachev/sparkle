package com.example.sparkle.sparkle.config;


import jakarta.servlet.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestAttributeCspNonceFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Генерируем nonce и сохраняем в атрибутах запроса
        String nonce = UUID.randomUUID().toString();
        request.setAttribute("cspNonce", nonce);
        chain.doFilter(request, response);
    }
}
