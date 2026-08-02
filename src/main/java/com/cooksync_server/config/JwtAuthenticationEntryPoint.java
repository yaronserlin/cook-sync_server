package com.cooksync_server.config;

import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.dtos.response.ApiResponse;
import com.dtos.response.errors.ApiErrorResponse;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Runs whenever an unauthenticated request reaches an endpoint that requires
 * authentication (missing, blank, expired, or otherwise invalid JWT). Without
 * this, Spring Security's default entry point returns a bare 403 with no
 * body, which the client can't distinguish from a real authorization failure.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws java.io.IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                "INVALID_OR_MISSING_TOKEN",
                "Authentication is required or the provided token is invalid/expired",
                request.getRequestURI(),
                null
        );

        objectMapper.writeValue(response.getWriter(), new ApiResponse<>(false, null, error, null));
    }
}
