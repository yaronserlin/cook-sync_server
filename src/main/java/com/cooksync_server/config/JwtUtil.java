package com.cooksync_server.config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Utility component responsible for all JSON Web Token (JWT) operations,
 * including generation, validation, parsing, and claim extraction.
 *
 * @author Yaron Serlin
 * @version Last Updated: 06/07/2026
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyString;

    private final long JWT_EXPIRATION = 1000 * 60 * 60 * 24;

    /**
     * Decodes the base64-encoded secret key property into a cryptographic Key
     * object.
     *
     * @return A {@link Key} instance used for signing and verifying JWTs using
     * HMAC-SHA.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the user's email (stored as the token's subject) from the
     * provided JWT.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * String email = jwtUtil.extractEmail(token);
     * }</pre>
     *
     * @param token The JWT string to parse.
     * @return The extracted email address.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims -> Claims.getSubject());
    }

    /**
     * Extracts the user's unique identifier from the provided JWT.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * String userId = jwtUtil.extractUserId(token);
     * }</pre>
     *
     * @param token The JWT string to parse.
     * @return The extracted user ID as a String.
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Checks whether the provided JWT has expired based on its expiration
     * claim.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * if (jwtUtil.isTokenExpired(token)) {
     * throw new ExpiredJwtException(...);
     * }
     * }</pre>
     *
     * @param token The JWT string to evaluate.
     * @return {@code true} if the token's expiration date is before the current
     * date, {@code false} otherwise.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the exact expiration date and time from the provided JWT.
     *
     * @param token The JWT string to parse.
     * @return A {@link Date} object representing when the token expires.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims -> Claims.getExpiration());
    }

    /**
     * Generic method to extract a specific claim from the JWT using a provided
     * resolver function.
     *
     * @param token The JWT string to parse.
     * @param claimsResolver A functional interface defining how to extract the
     * desired claim from the Claims object.
     * @param <T> The type of the claim to return.
     * @return The resolved claim of type T.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT to extract all stored claims, verifying the token's
     * signature in the process.
     *
     * @param token The JWT string to parse.
     * @return A {@link Claims} object containing all standard and custom
     * claims.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Generates a new JWT containing the user's basic profile details as custom
     * claims.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * String token = jwtUtil.generateToken("alice@example.com", "uuid-1234", true);
     * }</pre>
     *
     * @param email The user's email address, which serves as the token subject.
     * @param userId The user's unique identifier.
     * @param isAdmin A boolean indicating whether the user possesses
     * administrative privileges.
     * @return A fully constructed, signed, and compacted JWT string.
     */
    public String generateToken(String email, String userId, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("isAdmin", isAdmin);

        return createToken(claims, email);
    }

    /**
     * Internal builder method to construct, configure, and sign the JWT.
     *
     * @param claims A map of custom claims to embed in the token payload.
     * @param subject The primary subject of the token (typically the username
     * or email).
     * @return The built JWT string.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a token by comparing its subject email against the expected
     * email and ensuring it has not expired.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>{@code
     * boolean isValid = jwtUtil.validateToken(token, "alice@example.com");
     * }</pre>
     *
     * @param token The JWT string to validate.
     * @param userEmail The email address expected to be stored within the token
     * subject.
     * @return {@code true} if the token matches the user and is still actively
     * valid, {@code false} otherwise.
     */
    public boolean validateToken(String token, String userEmail) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(userEmail) && !isTokenExpired(token));
    }
}
