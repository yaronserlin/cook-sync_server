package com.cooksync_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration class for Spring Security, defining HTTP security rules, JWT
 * filter integration, and the password encoding mechanism.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Initializes the security configuration with the required JWT
     * authentication filter.
     *
     * @param jwtAuthFilter The custom filter used to intercept and validate
     * JWTs in incoming requests.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Configures the security filter chain, defining route permissions, session
     * management policies, and integrating the custom JWT authentication
     * filter.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * // Handled automatically by Spring Security context initialization.
     * }</pre>
     *
     * @param http The HttpSecurity object to configure web based security for
     * specific HTTP requests.
     * @return A constructed {@link SecurityFilterChain} object containing the
     * configured security rules.
     * @throws Exception if an error occurs during the configuration of the
     * security filter chain.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/recipes/public/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides the password encoder bean used for hashing and verifying
     * passwords securely.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * @Autowired
     * private PasswordEncoder passwordEncoder;
     * * String hashedPassword = passwordEncoder.encode("SecurePassword123!");
     * }</pre>
     *
     * @return A {@link PasswordEncoder} implementation, specifically utilizing
     * {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
