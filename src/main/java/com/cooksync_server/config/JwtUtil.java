package com.cooksync_server.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // מפתח סודי להצפנת הטוקן. בסביבת פרודקשן אמיתית זה צריך לשבת בקובץ properties/סביבה ולא בקוד!
    // יצרנו כאן מפתח ארוך ומאובטח שמתאים לאלגוריתם HS256
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // תוקף הטוקן: 24 שעות (באלפיות שנייה)
    private final long JWT_EXPIRATION = 1000 * 60 * 60 * 24;

    // חילוץ האימייל (Username) מהטוקן
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // חילוץ ה-ID של המשתמש מהטוקן
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    // בדיקה האם הטוקן פג תוקף
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // יצירת טוקן חדש (נקרא לזה מה-AuthService)
    public String generateToken(String email, String userId, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("isAdmin", isAdmin);

        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // לרוב שמים כאן את האימייל או שם המשתמש
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(SECRET_KEY)
                .compact();
    }

    // בדיקה סופית האם הטוקן תקין ושייך למשתמש
    public boolean validateToken(String token, String userEmail) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(userEmail) && !isTokenExpired(token));
    }
}
