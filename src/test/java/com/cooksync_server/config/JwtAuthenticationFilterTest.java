package com.cooksync_server.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Unit test for JwtAuthenticationFilter verifying boundary cases for token authentication.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
class JwtAuthenticationFilterTest {

    /**
     * Verifies that expired Bearer tokens pass through without throwing exceptions.
     *
     * @throws Exception if filter execution fails
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void doesNotThrowWhenBearerTokenIsExpired() throws Exception {
        JwtUtil jwtUtil = new JwtUtil();
        String secret = Base64.getEncoder().encodeToString("test-secret-key-1234567890-1234567890".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secret);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);

        String expiredToken = Jwts.builder()
                .setSubject("user@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 60_000))
                .setExpiration(new Date(System.currentTimeMillis() - 10_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)), SignatureAlgorithm.HS256)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + expiredToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        SecurityContextHolder.clearContext();

        assertThatCode(() -> filter.doFilter(request, response, chain)).doesNotThrowAnyException();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /**
     * Verifies that empty Bearer headers pass through safely.
     *
     * @throws Exception if filter execution fails
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     */
    @Test
    void doesNotThrowWhenBearerTokenIsBlank() throws Exception {
        JwtUtil jwtUtil = new JwtUtil();
        String secret = Base64.getEncoder().encodeToString("test-secret-key-1234567890-1234567890".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secret);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        SecurityContextHolder.clearContext();

        assertThatCode(() -> filter.doFilter(request, response, chain)).doesNotThrowAnyException();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
