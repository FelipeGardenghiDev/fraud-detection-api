package com.felipedev.frauddetection.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-KEY";
    private final String validApiKey;

    public ApiKeyAuthFilter(String validApiKey) {
        this.validApiKey = validApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Ignora rotas públicas como Swagger, Actuator e H2 Console
        if (!requestPath.startsWith("/api/v1/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestApiKey = request.getHeader(API_KEY_HEADER);

        if (requestApiKey == null || !requestApiKey.equals(validApiKey)) {
            log.warn("Tentativa de acesso não autorizada a {}. Header {} inválido ou ausente.", requestPath, API_KEY_HEADER);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                {
                    "type": "https://api.frauddetection.com/errors/unauthorized",
                    "title": "Acesso Não Autorizado",
                    "status": 401,
                    "detail": "Cabeçalho X-API-KEY ausente ou inválido para comunicação com este microsserviço."
                }
            """);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(new ApiKeyAuthenticationToken(requestApiKey));
        filterChain.doFilter(request, response);
    }
}