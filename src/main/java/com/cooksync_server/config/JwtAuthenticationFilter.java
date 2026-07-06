package com.cooksync_server.config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Component filter that intercepts incoming HTTP requests to extract and
 * validate JSON Web Tokens (JWT). Populates the Spring Security context with
 * user authentication details and authorization roles if a valid token is
 * present.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * Initializes the authentication filter with the required JWT utility
     * component.
     *
     * @param jwtUtil Utility component responsible for parsing, validating, and
     * extracting claims from JWTs.
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Inspects the Authorization header of incoming requests for a valid Bearer
     * token. If a valid token is found, establishes an authenticated security
     * context for the duration of the request.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * // This method is automatically invoked once per request by the Spring Security filter chain.
     * // No manual invocation is required.
     * }</pre>
     *
     * @param request The incoming HTTP servlet request to be intercepted and
     * inspected.
     * @param response The outgoing HTTP servlet response.
     * @param filterChain The chain of filters through which the request and
     * response are passed after execution.
     * @throws ServletException if a servlet-specific error occurs during the
     * filtering process.
     * @throws IOException if an input or output exception occurs during the
     * filtering process.
     */
        @Override
        protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
        ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtUtil.extractEmail(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtUtil.validateToken(jwt, userEmail)) {

                    boolean isAdmin = jwtUtil.extractClaim(jwt, claims -> claims.get("isAdmin", Boolean.class));
                    String role = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            System.out.println("Invalid JWT Token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
